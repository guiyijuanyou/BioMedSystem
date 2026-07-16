package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*; import java.util.*;

@Service
public class MultiMetricEvaluationService {
    private final JdbcTemplate jdbc; private final PermissionService permissions; private final QualityMetricService metricService;
    public MultiMetricEvaluationService(JdbcTemplate jdbc, PermissionService permissions, QualityMetricService metricService) {
        this.jdbc = jdbc; this.permissions = permissions; this.metricService = metricService;
    }

    /** 概览：方案列表、批次列表、评价历史 */
    public Map<String, Object> overview(PermissionService.Actor actor) {
        professional(actor);
        return Map.of(
            "schemes", jdbc.queryForList(
                "SELECT s.id, s.scheme_code AS schemeCode, s.scheme_name AS schemeName, " +
                "s.evaluation_type AS evaluationType, s.passing_score AS passingScore, " +
                "s.version_no AS versionNo, s.status, " +
                "COUNT(i.id) AS itemCount " +
                "FROM evaluation_scheme s " +
                "LEFT JOIN evaluation_scheme_item i ON i.scheme_id = s.id " +
                "GROUP BY s.id ORDER BY s.scheme_code, s.version_no DESC"),
            "batches", jdbc.queryForList(
                "SELECT b.id, b.batch_code AS batchCode, b.batch_name AS batchName, " +
                "h.name AS herbName, h.id AS herbId " +
                "FROM herb_batch b JOIN herb h ON h.id = b.herb_id " +
                "WHERE b.status = 'active' ORDER BY b.created_at DESC"),
            "evaluations", jdbc.queryForList(
                "SELECT e.id, e.scheme_id AS schemeId, s.scheme_name AS schemeName, " +
                "e.batch_id AS batchId, b.batch_name AS batchName, h.name AS herbName, " +
                "e.total_score AS totalScore, e.grade_name AS gradeName, e.result, e.conclusion, " +
                "e.evaluator_name AS evaluator, e.created_at AS createdAt " +
                "FROM multi_metric_evaluation e " +
                "JOIN evaluation_scheme s ON s.id = e.scheme_id " +
                "JOIN herb_batch b ON b.id = e.batch_id " +
                "JOIN herb h ON h.id = b.herb_id " +
                "ORDER BY e.created_at DESC LIMIT 100")
        );
    }

    /** 评价详情 */
    public Map<String, Object> detail(String id, PermissionService.Actor actor) {
        professional(actor);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("evaluation", required(
            "SELECT e.*, s.scheme_name, b.batch_name, h.name AS herb_name " +
            "FROM multi_metric_evaluation e " +
            "JOIN evaluation_scheme s ON s.id = e.scheme_id " +
            "JOIN herb_batch b ON b.id = e.batch_id " +
            "JOIN herb h ON h.id = b.herb_id " +
            "WHERE e.id = ?", id));
        out.put("details", jdbc.queryForList(
            "SELECT metric_name AS metricName, metric_version AS metricVersion, " +
            "measured_value AS measuredValue, unit_name AS unitName, judgement, " +
            "raw_score AS rawScore, weight_value AS weightValue, " +
            "weighted_score AS weightedScore, source_count AS sourceCount " +
            "FROM multi_metric_evaluation_detail WHERE evaluation_id = ? ORDER BY metric_name", id));
        return out;
    }

    /**
     * 执行自动评价
     *
     * 流程:
     *   1. 加载方案 → 获取要评价的 metric_code 列表
     *   2. 加载批次 → 获取 herb_id
     *   3. 按 (herb_id + metric_code) 逐一匹配用户在"指标标准"中配置的定义
     *   4. 从数据源聚合实测值
     *   5. 逐项评分（区间/权重全部来自指标标准定义）
     *   6. 归一化总分 → 判定等级 → 持久化
     */
    @Transactional
    public Map<String, Object> evaluate(String schemeId, String batchId, PermissionService.Actor actor) {
        professional(actor);

        // ── Step 1: 加载方案 → metric_code 列表 ──
        Map<String, Object> scheme = required(
            "SELECT * FROM evaluation_scheme WHERE id = ? AND status = 'active'", schemeId);
        List<Map<String, Object>> schemeItems = jdbc.queryForList(
            "SELECT metric_code FROM evaluation_scheme_item WHERE scheme_id = ?", schemeId);
        if (schemeItems.isEmpty())
            throw new IllegalArgumentException("评价方案「" + scheme.get("scheme_name") + "」未配置指标项");

        // ── Step 2: 加载批次 → herb_id + herb_name ──
        Map<String, Object> batch = required(
            "SELECT b.*, h.name AS herb_name " +
            "FROM herb_batch b JOIN herb h ON h.id = b.herb_id WHERE b.id = ?", batchId);
        String herbId   = String.valueOf(batch.get("herb_id"));
        String herbName = String.valueOf(batch.get("herb_name"));

        // ── Step 3: 按 (herb_id + metric_code) 匹配指标标准 ──
        List<Map<String, Object>> matchedDefs = new ArrayList<>();
        List<String> metricCodes = new ArrayList<>();
        for (Map<String, Object> item : schemeItems) {
            String code = String.valueOf(item.get("metric_code"));
            metricCodes.add(code);
            try {
                matchedDefs.add(metricService.findDefinition(code, herbId));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "药材「" + herbName + "」尚未配置指标「" + code + "」的评价标准，" +
                    "请先到「指标标准」页面为该药材配置此指标后再执行评价");
            }
        }

        // ── Step 4: 计算实测值 ──
        metricService.calculateForDefinitions(batchId, matchedDefs, actor);

        // ── Step 5: 逐项评分 ──
        // 权重、区间统一来自 quality_metric_definition（用户在"指标标准"中配置）
        BigDecimal totalScore  = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        List<Map<String, Object>> snapshots = new ArrayList<>();
        List<Map<String, Object>> matchedMetrics = new ArrayList<>();

        for (int i = 0; i < matchedDefs.size(); i++) {
            Map<String, Object> def  = matchedDefs.get(i);
            String metricCode = metricCodes.get(i);

            // 从 batch_metric_result 取刚计算的结果
            List<Map<String, Object>> rs = jdbc.queryForList(
                "SELECT * FROM batch_metric_result " +
                "WHERE batch_id = ? AND metric_code = ? " +
                "ORDER BY calculated_at DESC LIMIT 1",
                batchId, metricCode);

            if (rs.isEmpty() || rs.get(0).get("measured_value") == null)
                throw new IllegalArgumentException(
                    "指标「" + def.get("metricName") + "」的实测数据缺失，" +
                    "请确认该批次有对应的" + def.get("sourceType") + "数据");

            Map<String, Object> r   = rs.get(0);
            BigDecimal raw           = decimal(r.get("score"));
            BigDecimal weight        = decimal(def.get("weightValue"));
            BigDecimal weighted      = raw.multiply(weight)
                                           .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalScore  = totalScore.add(weighted);
            totalWeight = totalWeight.add(weight);

            // 快照（用于持久化）
            Map<String, Object> snap = new LinkedHashMap<>();
            snap.put("definitionId", def.get("id"));
            snap.put("metricCode", metricCode);
            snap.put("metricName", def.get("metricName"));
            snap.put("versionNo", def.get("versionNo"));
            snap.put("result", r);
            snap.put("raw", raw);
            snap.put("weighted", weighted);
            snap.put("weightValue", def.get("weightValue"));
            snapshots.add(snap);

            // 匹配指标卡片（返回给前端展示）
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("metricCode", metricCode);
            mm.put("metricName", def.get("metricName"));
            mm.put("unitName", def.get("unitName"));
            mm.put("weightValue", def.get("weightValue"));
            mm.put("minimumValue", def.get("minimumValue"));
            mm.put("maximumValue", def.get("maximumValue"));
            mm.put("targetValue", def.get("targetValue"));
            matchedMetrics.add(mm);
        }

        // ── Step 6: 归一化 & 判定 ──
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            totalScore = totalScore.multiply(BigDecimal.valueOf(100))
                                   .divide(totalWeight, 2, RoundingMode.HALF_UP);
        }

        BigDecimal passing = decimal(scheme.get("passing_score"));
        String result = totalScore.compareTo(passing) >= 0 ? "qualified" : "unqualified";
        String grade  = totalScore.compareTo(BigDecimal.valueOf(90)) >= 0 ? "excellent"
                      : totalScore.compareTo(BigDecimal.valueOf(80)) >= 0 ? "good"
                      : totalScore.compareTo(passing) >= 0 ? "qualified"
                      : "unqualified";

        // ── Step 7: 持久化 ──
        String id       = UUID.randomUUID().toString();
        String recordId = UUID.randomUUID().toString();
        String conclusion = "自动评价：" + grade + "，综合得分 " + totalScore;

        jdbc.update(
            "INSERT INTO evaluation_record (id,batch_id,herb_name,indicator,score,result," +
            "application_material,subject_owner_name,evaluator_name,evaluator_role," +
            "status,version,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,0,NOW())",
            recordId, batchId, herbName, scheme.get("scheme_name"), totalScore, result,
            "multi_metric_evaluation:" + id, batch.get("responsible_person"),
            actor.name(), actor.role(), "草稿");

        jdbc.update(
            "INSERT INTO multi_metric_evaluation (id,scheme_id,scheme_version,batch_id," +
            "evaluation_record_id,total_score,grade_name,result,conclusion,evaluator_name) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)",
            id, schemeId, scheme.get("version_no"), batchId, recordId,
            totalScore, grade, result, conclusion, actor.name());

        for (Map<String, Object> snap : snapshots) {
            @SuppressWarnings("unchecked")
            Map<String, Object> r = (Map<String, Object>) snap.get("result");
            jdbc.update(
                "INSERT INTO multi_metric_evaluation_detail (id,evaluation_id," +
                "metric_definition_id,metric_code,metric_name,metric_version," +
                "measured_value,unit_name,judgement,raw_score,weight_value," +
                "weighted_score,source_result_id,source_count) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                UUID.randomUUID().toString(), id,
                snap.get("definitionId"), snap.get("metricCode"), snap.get("metricName"),
                snap.get("versionNo"),
                r.get("measured_value"), r.get("unit_name"), r.get("judgement"),
                snap.get("raw"), snap.get("weightValue"), snap.get("weighted"),
                r.get("id"), r.get("source_count"));
        }

        // 返回完整结果
        Map<String, Object> out = detail(id, actor);
        out.put("matchedHerb", Map.of("herbId", herbId, "herbName", herbName));
        out.put("matchedMetrics", matchedMetrics);
        return out;
    }

    /** 删除评价（仅管理员） */
    @Transactional
    public void delete(String id, PermissionService.Actor actor) {
        if (!permissions.isAdmin(actor))
            throw new AuthorizationDeniedException("only admin can delete automatic evaluations");
        Map<String, Object> e = required(
            "SELECT evaluation_record_id FROM multi_metric_evaluation WHERE id = ?", id);
        jdbc.update("DELETE FROM multi_metric_evaluation_detail WHERE evaluation_id = ?", id);
        jdbc.update("DELETE FROM multi_metric_evaluation WHERE id = ?", id);
        if (e.get("evaluation_record_id") != null)
            jdbc.update("DELETE FROM evaluation_record WHERE id = ? AND status = '草稿'",
                e.get("evaluation_record_id"));
    }

    // ── helpers ──

    private void professional(PermissionService.Actor a) {
        if (permissions.isStudent(a))
            throw new AuthorizationDeniedException("students cannot maintain professional evaluations");
    }

    private Map<String, Object> required(String sql, Object... args) {
        List<Map<String, Object>> r = jdbc.queryForList(sql, args);
        if (r.isEmpty()) throw new IllegalArgumentException("record not found");
        return r.get(0);
    }

    private BigDecimal decimal(Object v) {
        return v == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(v));
    }
}
