package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class QualityMetricService {
    private final JdbcTemplate jdbc;
    private final PermissionService permissions;

    public QualityMetricService(JdbcTemplate jdbc, PermissionService permissions) {
        this.jdbc = jdbc;
        this.permissions = permissions;
    }

    // ── 数据源聚合白名单 ──
    private record AggConfig(String table, String column, String aggFunc, String label) {}

    private static final Map<String, AggConfig> AGG = Map.of(
        "temperature_avg", new AggConfig("growth_record", "temperature", "AVG", "生长记录-温度"),
        "humidity_avg",    new AggConfig("growth_record", "humidity",    "AVG", "生长记录-湿度"),
        "soil_ph_avg",     new AggConfig("growth_record", "soil_ph",     "AVG", "生长记录-土壤pH"),
        "similarity_max",  new AggConfig("spectrum_comparison", "similarity", "MAX", "图谱比对-相似度")
    );

    // ── 概览 ──

    /** 返回所有药材 + 各自的指标配置 + 批次列表 */
    public Map<String, Object> overview(PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) throw new AuthorizationDeniedException("students cannot manage quality metrics");
        List<Map<String, Object>> herbs = jdbc.queryForList("SELECT id, name AS herbName FROM herb ORDER BY name");
        List<Map<String, Object>> configs = jdbc.queryForList(
                "SELECT id, herb_id AS herbId, metric_code AS metricCode, metric_name AS metricName, " +
                "source_type AS sourceType, source_field AS sourceField, unit_name AS unitName, " +
                "minimum_value AS minimumValue, maximum_value AS maximumValue, target_value AS targetValue, " +
                "weight_value AS weightValue, version_no AS versionNo, status " +
                "FROM quality_metric_definition WHERE status='active' ORDER BY herb_id, metric_name");
        // 按 herbId 分组
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> c : configs) {
            String herbId = String.valueOf(c.get("herbId"));
            grouped.computeIfAbsent(herbId, k -> new ArrayList<>()).add(c);
        }
        return Map.of(
                "herbs", herbs,
                "configs", grouped,
                "batches", jdbc.queryForList("SELECT b.id,b.batch_code AS batchCode,b.batch_name AS batchName,h.name AS herbName,h.id AS herbId FROM herb_batch b JOIN herb h ON h.id=b.herb_id WHERE b.status='active' ORDER BY b.created_at DESC"),
                "results", jdbc.queryForList("SELECT r.id,r.batch_id AS batchId,r.metric_name AS metricName,r.measured_value AS measuredValue,r.unit_name AS unitName,r.judgement,r.score,r.source_count AS sourceCount,r.calculation_note AS calculationNote,r.calculated_at AS calculatedAt FROM batch_metric_result r ORDER BY r.calculated_at DESC LIMIT 200")
        );
    }

    // ── 指标标准配置（用户在"指标标准"模块自行维护） ──

    /** 保存/更新某药材的全部指标配置 */
    @Transactional
    public Map<String, Object> saveHerbConfig(String herbId, Map<String, Object> body, PermissionService.Actor actor) {
        if (!permissions.isAdmin(actor)) throw new AuthorizationDeniedException("only admin can maintain metric standards");
        jdbc.queryForMap("SELECT id FROM herb WHERE id=?", herbId); // 验证药材存在

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> metrics = (Map<String, Map<String, Object>>) body.get("metrics");
        if (metrics == null || metrics.isEmpty()) throw new IllegalArgumentException("metrics is required");

        List<Map<String, Object>> saved = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : metrics.entrySet()) {
            String code = entry.getKey();
            Map<String, Object> cfg = entry.getValue();
            String name = String.valueOf(cfg.getOrDefault("metricName", code));
            String sourceType = String.valueOf(cfg.getOrDefault("sourceType", "growth"));
            String sourceField = String.valueOf(cfg.getOrDefault("sourceField", ""));
            String unit = String.valueOf(cfg.getOrDefault("unitName", ""));
            if ("null".equals(unit) || unit.isBlank()) unit = null;

            BigDecimal min = decimal(cfg.get("minimumValue"));
            BigDecimal max = decimal(cfg.get("maximumValue"));
            BigDecimal target = decimal(cfg.get("targetValue"));
            BigDecimal weight = cfg.get("weightValue") != null ? decimal(cfg.get("weightValue")) : BigDecimal.ONE;

            // 查找已有版本
            Integer nextVersion = jdbc.queryForObject(
                    "SELECT COALESCE(MAX(version_no),0)+1 FROM quality_metric_definition WHERE metric_code=? AND herb_id=?",
                    Integer.class, code, herbId);

            // 停用旧版本
            jdbc.update("UPDATE quality_metric_definition SET status='inactive' WHERE metric_code=? AND herb_id=? AND status='active'",
                    code, herbId);

            // 插入新版本
            String id = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO quality_metric_definition (id,metric_code,metric_name,herb_id,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    id, code, name, herbId, sourceType, sourceField, unit, min, max, target, weight, nextVersion, "active", actor.name());

            saved.add(jdbc.queryForMap("SELECT id, herb_id AS herbId, metric_code AS metricCode, metric_name AS metricName, source_type AS sourceType, source_field AS sourceField, unit_name AS unitName, minimum_value AS minimumValue, maximum_value AS maximumValue, target_value AS targetValue, weight_value AS weightValue, version_no AS versionNo, status FROM quality_metric_definition WHERE id=?", id));
        }
        return Map.of("herbId", herbId, "metrics", saved);
    }

    /** 获取某药材的活跃指标配置（供数据分析和评价使用） */
    public List<Map<String, Object>> getHerbMetrics(String herbId) {
        return jdbc.queryForList(
                "SELECT id, herb_id AS herbId, metric_code AS metricCode, metric_name AS metricName, " +
                "source_type AS sourceType, source_field AS sourceField, unit_name AS unitName, " +
                "minimum_value AS minimumValue, maximum_value AS maximumValue, target_value AS targetValue, " +
                "weight_value AS weightValue, version_no AS versionNo " +
                "FROM quality_metric_definition WHERE herb_id=? AND status='active' ORDER BY metric_name", herbId);
    }

    /** 按 metric_code + herb_id 查找单条指标定义（自动评价匹配用） */
    public Map<String, Object> findDefinition(String metricCode, String herbId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, herb_id AS herbId, metric_code AS metricCode, metric_name AS metricName, " +
                "source_type AS sourceType, source_field AS sourceField, unit_name AS unitName, " +
                "minimum_value AS minimumValue, maximum_value AS maximumValue, target_value AS targetValue, " +
                "weight_value AS weightValue, version_no AS versionNo, status " +
                "FROM quality_metric_definition " +
                "WHERE metric_code=? AND herb_id=? AND status='active' ORDER BY version_no DESC LIMIT 1",
                metricCode, herbId);
        if (rows.isEmpty()) throw new IllegalArgumentException(
                "未找到药材 " + herbId + " 的指标 " + metricCode + "，请先在「指标标准」中为该药材配置此指标");
        return rows.get(0);
    }

    // ── 指标计算 ──

    /**
     * 计算单批次全部已配置指标（用户在"指标标准"中为该药材配置的所有活跃指标）
     * 先删后插，幂等
     */
    @Transactional
    public List<Map<String, Object>> calculate(String batchId, PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) throw new AuthorizationDeniedException("students cannot calculate quality metrics");
        Map<String, Object> batch = requiredRow("SELECT herb_id FROM herb_batch WHERE id=?", batchId);
        String herbId = String.valueOf(batch.get("herb_id"));
        List<Map<String, Object>> definitions = getHerbMetrics(herbId);
        if (definitions.isEmpty()) throw new IllegalArgumentException(
                "该批次对应的药材尚未配置任何评价指标，请先到「指标标准」页面为该药材配置指标");
        return calculateBatch(batchId, definitions, actor);
    }

    /**
     * 按给定的指标定义列表计算（自动评价时使用，只计算方案需要的指标）
     */
    @Transactional
    public List<Map<String, Object>> calculateForDefinitions(String batchId, List<Map<String, Object>> definitions, PermissionService.Actor actor) {
        return calculateBatch(batchId, definitions, actor);
    }

    private List<Map<String, Object>> calculateBatch(String batchId, List<Map<String, Object>> definitions, PermissionService.Actor actor) {
        // 先删该批次旧结果
        jdbc.update("DELETE FROM batch_metric_result WHERE batch_id=?", batchId);
        List<Map<String, Object>> output = new ArrayList<>();
        for (Map<String, Object> def : definitions) {
            String sourceField = String.valueOf(def.get("sourceField"));
            Measurement m = measure(batchId, sourceField);
            String judgement = judge(m.value(), number(def.get("minimumValue")), number(def.get("maximumValue")));
            BigDecimal score = score(m.value(), number(def.get("minimumValue")), number(def.get("maximumValue")), number(def.get("targetValue")));
            String id = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO batch_metric_result (id,batch_id,metric_definition_id,metric_code,metric_name,measured_value,unit_name,judgement,score,source_count,calculation_note,calculated_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    id, batchId, def.get("id"), def.get("metricCode"), def.get("metricName"),
                    m.value(), def.get("unitName"), judgement, score, m.count(), m.note(), actor.name());
            output.add(jdbc.queryForMap("SELECT id,batch_id AS batchId,metric_name AS metricName,measured_value AS measuredValue,unit_name AS unitName,judgement,score,source_count AS sourceCount,calculation_note AS calculationNote FROM batch_metric_result WHERE id=?", id));
        }
        return output;
    }

    // ── 数据聚合 ──

    /**
     * 按 sourceField 从对应数据表聚合实测值
     * 使用白名单 AGG 控制表名/列名，安全且可扩展
     */
    private Measurement measure(String batchId, String sourceField) {
        AggConfig c = AGG.get(sourceField);
        if (c == null) throw new IllegalArgumentException("不支持的指标来源字段: " + sourceField);

        String sql = String.format("SELECT %s(%s), COUNT(%s) FROM %s WHERE batch_id = ?",
                c.aggFunc, c.column, c.column, c.table);

        return jdbc.queryForObject(sql, (rs, row) -> {
            BigDecimal val = rs.getBigDecimal(1);
            int count = rs.getInt(2);
            if (val == null || count == 0) {
                throw new IllegalArgumentException(
                        "该批次在「" + c.label + "」中暂无数据，请先录入数据后再执行评价");
            }
            return new Measurement(val.setScale(4, RoundingMode.HALF_UP), count,
                    c.table + " " + c.aggFunc + "(" + c.column + ")");
        }, batchId);
    }

    // ── 判定与评分 ──

    private String judge(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) return "no_data";
        if (min != null && value.compareTo(min) < 0) return "below_standard";
        if (max != null && value.compareTo(max) > 0) return "above_standard";
        return "qualified";
    }

    private BigDecimal score(BigDecimal value, BigDecimal min, BigDecimal max, BigDecimal target) {
        if (value == null) return null;
        // 图谱指标：区间为全局统一值
        if (min == null && max == null) { min = new BigDecimal("90"); max = new BigDecimal("100"); }
        if (target == null) target = min.add(max).divide(BigDecimal.valueOf(2));
        if (min != null && value.compareTo(min) < 0)
            return min.signum() == 0 ? BigDecimal.ZERO : value.divide(min, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(79)).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        if (max != null && value.compareTo(max) > 0) {
            BigDecimal span = max.subtract(target).abs().max(BigDecimal.ONE);
            return BigDecimal.valueOf(79).subtract(value.subtract(max).divide(span, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(30))).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal distance = value.compareTo(target) <= 0 && min != null ? target.subtract(min) : (max == null ? BigDecimal.ONE : max.subtract(target));
        if (distance.signum() <= 0) return BigDecimal.valueOf(100);
        return BigDecimal.valueOf(100).subtract(value.subtract(target).abs().divide(distance, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(20))).max(BigDecimal.valueOf(80)).setScale(2, RoundingMode.HALF_UP);
    }

    // ── 工具方法 ──

    private Map<String, Object> requiredRow(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) throw new IllegalArgumentException("record not found");
        return rows.get(0);
    }
    private BigDecimal decimal(Object v) { if (v == null || String.valueOf(v).isBlank()) return null; return new BigDecimal(String.valueOf(v)); }
    private BigDecimal number(Object v) { return v == null ? null : new BigDecimal(String.valueOf(v)); }
    private record Measurement(BigDecimal value, int count, String note) {}
}
