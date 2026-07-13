package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.GrowthAnalysis;
import com.cqutcm.biomed.entity.SpectrumComparison;
import com.cqutcm.biomed.mapper.GrowthAnalysisMapper;
import com.cqutcm.biomed.mapper.SpectrumComparisonMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResearchDataService {
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;
    private final PermissionService permissionService;
    private final BatchCatalogService batchCatalogService;

    public ResearchDataService(SpectrumComparisonMapper spectrumMapper,
                               GrowthAnalysisMapper analysisMapper,
                               PermissionService permissionService,
                               BatchCatalogService batchCatalogService) {
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
        this.permissionService = permissionService;
        this.batchCatalogService = batchCatalogService;
    }

    // ---- 列表 ----

    public List<Map<String, Object>> listSpectrum() {
        return spectrumMapper.findAllAsMap().stream().map(this::spectrumView).toList();
    }

    public List<Map<String, Object>> listAnalysis() {
        return analysisMapper.findAllAsMap().stream().map(this::analysisView).toList();
    }

    public List<Map<String, Object>> listSpectrumForActor(PermissionService.Actor actor) {
        List<Map<String, Object>> all = listSpectrum();
        if (permissionService.isStudent(actor)) {
            return all.stream()
                    .filter(s -> StatusMachine.isStudentVisible(str(s, "status")))
                    .collect(Collectors.toList());
        }
        return all;
    }

    public List<Map<String, Object>> listAnalysisForActor(PermissionService.Actor actor) {
        List<Map<String, Object>> all = listAnalysis();
        if (permissionService.isStudent(actor)) {
            return all.stream()
                    .filter(a -> StatusMachine.isStudentVisible(str(a, "status")))
                    .collect(Collectors.toList());
        }
        return all;
    }

    // ---- 创建 ----

    public Map<String, Object> createSpectrum(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        batchCatalogService.applySampleContext(cleaned);
        // 创建人由登录会话强制确定，防伪造
        if (!permissionService.isAdmin(actor)) {
            cleaned.put("operatorName", actor.name());
            cleaned.put("status", StatusMachine.initialStatus(false));
        } else {
            cleaned.putIfAbsent("status", StatusMachine.initialStatus(true));
        }
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        spectrumMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> createAnalysis(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        batchCatalogService.applyBatchContext(cleaned);
        // 分析人由登录会话强制确定
        if (!permissionService.isAdmin(actor)) {
            cleaned.put("analystName", actor.name());
            cleaned.put("status", StatusMachine.initialStatus(false));
        } else {
            cleaned.putIfAbsent("status", StatusMachine.initialStatus(true));
        }
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        analysisMapper.insertMap(cleaned);
        return cleaned;
    }

    // ---- 更新 ----

    public Map<String, Object> updateSpectrum(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = id(payload, "spectrum comparison");
        Map<String, Object> existing = spectrumMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("spectrum comparison not found");

        // 非管理员仅可修改自身记录
        permissionService.assertResearchDataOwnership(actor, str(existing, "operatorName"));
        if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(str(existing, "status"), "图谱比对");

        // 审核后锁定算法结果/相似度
        if (StatusMachine.isLocked(str(existing, "status")) && !permissionService.isAdmin(actor)) {
            throw new AuthorizationDeniedException("审核后图谱比对结果已锁定，仅管理员可修改");
        }

        Map<String, Object> cleaned = clean(payload);
        cleaned.putIfAbsent("batchId", existing.get("batchId"));
        cleaned.putIfAbsent("sampleId", existing.get("sampleId"));
        batchCatalogService.applySampleContext(cleaned);
        cleaned.put("status", existing.get("status"));
        if (!permissionService.isAdmin(actor)) {
            // 保留操作人和状态
            cleaned.put("operatorName", existing.get("operatorName"));
            cleaned.put("status", existing.get("status"));
            // 禁止修改审核字段
            cleaned.remove("reviewerName");
            cleaned.remove("reviewComment");
            cleaned.remove("reviewedAt");
            // 审核锁定后禁止修改相似度和结果
            if (StatusMachine.isLocked(str(existing, "status"))) {
                cleaned.remove("similarity");
                cleaned.remove("result");
            }
        }
        cleaned.put("id", id);
        cleaned.put("version", existing.getOrDefault("version", 0));
        cleaned.put("reviewerName", existing.get("reviewerName"));
        cleaned.put("reviewComment", existing.get("reviewComment"));
        cleaned.put("reviewedAt", existing.get("reviewedAt"));
        if (spectrumMapper.updateMap(cleaned) == 0) {
            throw new StateConflictException("图谱比对记录已被其他用户修改，请刷新后重试");
        }
        cleaned.put("version", ((Number) cleaned.get("version")).intValue() + 1);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public Map<String, Object> updateAnalysis(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = id(payload, "growth analysis");
        Map<String, Object> existing = analysisMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("growth analysis not found");

        // 非管理员仅可修改自身记录
        permissionService.assertResearchDataOwnership(actor, str(existing, "analystName"));
        if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(str(existing, "status"), "数据分析");

        // 审核后锁定分析结论
        if (StatusMachine.isLocked(str(existing, "status")) && !permissionService.isAdmin(actor)) {
            throw new AuthorizationDeniedException("审核后生长分析结论已锁定，仅管理员可修改");
        }

        Map<String, Object> cleaned = clean(payload);
        cleaned.putIfAbsent("batchId", existing.get("batchId"));
        batchCatalogService.applyBatchContext(cleaned);
        cleaned.put("status", existing.get("status"));
        if (!permissionService.isAdmin(actor)) {
            cleaned.put("analystName", existing.get("analystName"));
            cleaned.put("status", existing.get("status"));
            cleaned.remove("reviewerName");
            cleaned.remove("reviewComment");
            cleaned.remove("reviewedAt");
            if (StatusMachine.isLocked(str(existing, "status"))) {
                cleaned.remove("conclusion");
                cleaned.remove("trend");
            }
        }
        cleaned.put("id", id);
        cleaned.put("version", existing.getOrDefault("version", 0));
        cleaned.put("reviewerName", existing.get("reviewerName"));
        cleaned.put("reviewComment", existing.get("reviewComment"));
        cleaned.put("reviewedAt", existing.get("reviewedAt"));
        if (analysisMapper.updateMap(cleaned) == 0) {
            throw new StateConflictException("数据分析记录已被其他用户修改，请刷新后重试");
        }
        cleaned.put("version", ((Number) cleaned.get("version")).intValue() + 1);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    // ---- 审核 ----

    public Map<String, Object> reviewSpectrum(String id, PermissionService.Actor actor,
                                               String targetStatus, String comment) {
        StatusMachine.assertRequireAdmin(actor.role());
        Map<String, Object> existing = spectrumMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("spectrum comparison not found");
        StatusMachine.assertTransition(str(existing, "status"), targetStatus, "图谱比对");

        SpectrumComparison entity = mapSpectrumEntity(existing);
        entity.setStatus(targetStatus);
        entity.setReviewerName(actor.name());
        entity.setReviewComment(comment);
        entity.setReviewedAt(LocalDateTime.now());
        if (spectrumMapper.update(entity) == 0) throw conflict("图谱比对");

        Map<String, Object> result = new LinkedHashMap<>(existing);
        result.put("status", targetStatus);
        result.put("reviewerName", actor.name());
        result.put("reviewComment", comment);
        result.put("reviewedAt", LocalDateTime.now().toString());
        return result;
    }

    public Map<String, Object> reviewAnalysis(String id, PermissionService.Actor actor,
                                               String targetStatus, String comment) {
        StatusMachine.assertRequireAdmin(actor.role());
        Map<String, Object> existing = analysisMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("growth analysis not found");
        StatusMachine.assertTransition(str(existing, "status"), targetStatus, "生长分析");

        GrowthAnalysis entity = mapAnalysisEntity(existing);
        entity.setStatus(targetStatus);
        entity.setReviewerName(actor.name());
        entity.setReviewComment(comment);
        entity.setReviewedAt(LocalDateTime.now());
        if (analysisMapper.update(entity) == 0) throw conflict("数据分析");

        Map<String, Object> result = new LinkedHashMap<>(existing);
        result.put("status", targetStatus);
        result.put("reviewerName", actor.name());
        result.put("reviewComment", comment);
        result.put("reviewedAt", LocalDateTime.now().toString());
        return result;
    }

    public Map<String, Object> submitSpectrum(String id, PermissionService.Actor actor) {
        Map<String, Object> existing = spectrumMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("spectrum comparison not found");
        permissionService.assertResearchDataOwnership(actor, str(existing, "operatorName"));
        StatusMachine.assertTransition(str(existing, "status"), StatusMachine.PENDING_REVIEW, "图谱比对");
        SpectrumComparison entity = mapSpectrumEntity(existing);
        entity.setStatus(StatusMachine.PENDING_REVIEW);
        if (spectrumMapper.update(entity) == 0) throw conflict("图谱比对");
        return new LinkedHashMap<>(spectrumMapper.findByIdAsMap(id));
    }

    public Map<String, Object> submitAnalysis(String id, PermissionService.Actor actor) {
        Map<String, Object> existing = analysisMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("growth analysis not found");
        permissionService.assertResearchDataOwnership(actor, str(existing, "analystName"));
        StatusMachine.assertTransition(str(existing, "status"), StatusMachine.PENDING_REVIEW, "数据分析");
        GrowthAnalysis entity = mapAnalysisEntity(existing);
        entity.setStatus(StatusMachine.PENDING_REVIEW);
        if (analysisMapper.update(entity) == 0) throw conflict("数据分析");
        return new LinkedHashMap<>(analysisMapper.findByIdAsMap(id));
    }

    // ---- 删除（软删除或限管理员） ----

    public void deleteSpectrum(String id) {
        if (spectrumMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("spectrum comparison not found");
        }
    }

    public void deleteSpectrum(String id, PermissionService.Actor actor) {
        if (!permissionService.isAdmin(actor)) {
            Map<String, Object> existing = spectrumMapper.findByIdAsMap(id);
            if (existing == null) throw new IllegalArgumentException("spectrum comparison not found");
            permissionService.assertResearchDataOwnership(actor, str(existing, "operatorName"));
            StatusMachine.assertOwnerEditable(str(existing, "status"), "图谱比对");
        }
        if (spectrumMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("spectrum comparison not found");
        }
    }

    public void deleteAnalysis(String id) {
        if (analysisMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("growth analysis not found");
        }
    }

    public void deleteAnalysis(String id, PermissionService.Actor actor) {
        if (!permissionService.isAdmin(actor)) {
            Map<String, Object> existing = analysisMapper.findByIdAsMap(id);
            if (existing == null) throw new IllegalArgumentException("growth analysis not found");
            permissionService.assertResearchDataOwnership(actor, str(existing, "analystName"));
            StatusMachine.assertOwnerEditable(str(existing, "status"), "数据分析");
        }
        if (analysisMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("growth analysis not found");
        }
    }

    // ---- 计数 ----

    public long spectrumCount() { return spectrumMapper.count(); }
    public long analysisCount() { return analysisMapper.count(); }

    // ---- 私有方法 ----

    private String id(Map<String, Object> payload, String label) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) throw new IllegalArgumentException("missing id for " + label);
        return id;
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.remove("_actorName");
        cleaned.remove("_actorRole");
        if (cleaned.containsKey("operator") && !cleaned.containsKey("operatorName")) {
            cleaned.put("operatorName", cleaned.get("operator"));
        }
        if (cleaned.containsKey("analyst") && !cleaned.containsKey("analystName")) {
            cleaned.put("analystName", cleaned.get("analyst"));
        }
        if (cleaned.containsKey("difference") && !cleaned.containsKey("differenceDesc")) {
            cleaned.put("differenceDesc", cleaned.get("difference"));
        }
        // Pass through data point and algorithm fields
        if (cleaned.containsKey("dataPointsJson") && !cleaned.containsKey("sampleDataJson")) {
            cleaned.put("sampleDataJson", cleaned.get("dataPointsJson"));
        }
        cleaned.putIfAbsent("compareAlgorithm", "COSINE");
        return cleaned;
    }

    private SpectrumComparison mapSpectrumEntity(Map<String, Object> m) {
        SpectrumComparison s = new SpectrumComparison();
        s.setId(str(m, "id"));
        s.setHerbName(str(m, "herbName"));
        s.setSampleCode(str(m, "sampleCode"));
        s.setDistrict(str(m, "district"));
        s.setSpectrumType(str(m, "spectrumType"));
        s.setReferenceName(str(m, "referenceName"));
        try { s.setSimilarity(new java.math.BigDecimal(String.valueOf(m.getOrDefault("similarity", "0")))); } catch (Exception ignored) {}
        s.setResult(str(m, "result"));
        s.setOperatorName(str(m, "operatorName"));
        s.setRemark(str(m, "remark"));
        s.setStatus(str(m, "status"));
        s.setReviewerName(str(m, "reviewerName"));
        s.setReviewComment(str(m, "reviewComment"));
        s.setVersion(number(m, "version"));
        return s;
    }

    private GrowthAnalysis mapAnalysisEntity(Map<String, Object> m) {
        GrowthAnalysis a = new GrowthAnalysis();
        a.setId(str(m, "id"));
        a.setAnalysisName(str(m, "analysisName"));
        a.setHerbName(str(m, "herbName"));
        a.setDistrict(str(m, "district"));
        a.setIndicator(str(m, "indicator"));
        a.setBaseline(str(m, "baseline"));
        a.setCurrentValue(str(m, "currentValue"));
        a.setDifferenceDesc(str(m, "differenceDesc"));
        a.setTrend(str(m, "trend"));
        a.setConclusion(str(m, "conclusion"));
        a.setAnalystName(str(m, "analystName"));
        a.setStatus(str(m, "status"));
        a.setReviewerName(str(m, "reviewerName"));
        a.setReviewComment(str(m, "reviewComment"));
        a.setVersion(number(m, "version"));
        return a;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Integer number(Map<String, Object> m, String key) {
        Object value = m.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private StateConflictException conflict(String label) {
        return new StateConflictException(label + "记录已被其他用户修改，请刷新后重试");
    }

    private Map<String, Object> spectrumView(Map<String, Object> source) {
        Map<String, Object> view = new LinkedHashMap<>(source);
        view.put("operator", source.get("operatorName"));
        // Keep data JSON fields but don't send full data points in list views (too large)
        // Only include a flag indicating whether data is available
        String sampleData = String.valueOf(source.getOrDefault("sampleDataJson", ""));
        String refData = String.valueOf(source.getOrDefault("referenceDataJson", ""));
        view.put("hasDataPoints", sampleData != null && !"null".equals(sampleData) && !"[]".equals(sampleData) && sampleData.length() > 10);
        view.put("hasReferenceData", refData != null && !"null".equals(refData) && !"[]".equals(refData) && refData.length() > 10);
        // Don't include the full JSON in list view to reduce payload size
        view.remove("sampleDataJson");
        view.remove("referenceDataJson");
        view.put("compareAlgorithm", source.getOrDefault("compareAlgorithm", "COSINE"));
        return view;
    }

    private Map<String, Object> analysisView(Map<String, Object> source) {
        Map<String, Object> view = new LinkedHashMap<>(source);
        view.put("analyst", source.get("analystName"));
        view.put("difference", source.get("differenceDesc"));
        return view;
    }
}
