package com.cqutcm.biomed.service;

import com.cqutcm.biomed.dto.BatchCatalogDTOs;
import com.cqutcm.biomed.entity.Herb;
import com.cqutcm.biomed.entity.HerbBatch;
import com.cqutcm.biomed.entity.LabSample;
import com.cqutcm.biomed.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BatchCatalogService {
    private final HerbBatchMapper batchMapper;
    private final HerbMapper herbMapper;
    private final LabSampleMapper sampleMapper;
    private final GrowthRecordMapper growthMapper;
    private final TraceEventMapper traceMapper;
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;
    private final EvaluationRecordMapper evaluationMapper;
    private final PermissionService permissionService;

    public BatchCatalogService(HerbBatchMapper batchMapper, HerbMapper herbMapper,
                               LabSampleMapper sampleMapper, GrowthRecordMapper growthMapper,
                               TraceEventMapper traceMapper, SpectrumComparisonMapper spectrumMapper,
                               GrowthAnalysisMapper analysisMapper, EvaluationRecordMapper evaluationMapper,
                               PermissionService permissionService) {
        this.batchMapper = batchMapper;
        this.herbMapper = herbMapper;
        this.sampleMapper = sampleMapper;
        this.growthMapper = growthMapper;
        this.traceMapper = traceMapper;
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
        this.evaluationMapper = evaluationMapper;
        this.permissionService = permissionService;
    }

    public List<Map<String, Object>> listBatches() {
        return batchMapper.findAllAsMap();
    }

    public List<Map<String, Object>> listSamples() {
        return sampleMapper.findAllAsMap();
    }

    public Map<String, Object> getBatch(String id) {
        Map<String, Object> result = batchMapper.findByIdAsMap(id);
        if (result == null) throw new IllegalArgumentException("药材批次不存在");
        return result;
    }

    public Map<String, Object> getSample(String id) {
        Map<String, Object> result = sampleMapper.findByIdAsMap(id);
        if (result == null) throw new IllegalArgumentException("检测样本不存在");
        return result;
    }

    public Map<String, Object> getBatchOverview(String id, PermissionService.Actor actor) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("batch", getBatch(id));
        overview.put("growthRecords", growthMapper.findByBatchIdAsMap(id));
        overview.put("traceEvents", traceMapper.findByBatchIdAsMap(id));
        overview.put("samples", sampleMapper.findByBatchIdAsMap(id));
        overview.put("spectrumComparisons", visibleWorkflow(spectrumMapper.findByBatchIdAsMap(id), actor));
        overview.put("analyses", visibleWorkflow(analysisMapper.findByBatchIdAsMap(id), actor));
        overview.put("evaluations", visibleWorkflow(evaluationMapper.findByBatchIdAsMap(id), actor));
        overview.put("generatedAt", LocalDateTime.now().toString());
        return overview;
    }

    @Transactional
    public Map<String, Object> createBatch(BatchCatalogDTOs.BatchCreate dto, PermissionService.Actor actor) {
        requireHerb(dto.herbId);
        assertUniqueBatchCode(dto.batchCode, null);
        HerbBatch batch = new HerbBatch();
        batch.setId(UUID.randomUUID().toString());
        copyBatch(dto, batch);
        batch.setResponsiblePerson(valueOrActor(dto.responsiblePerson, actor));
        batch.setCurrentStage(defaultValue(dto.currentStage, "未开始"));
        batch.setStatus("active");
        batch.setVersion(0);
        batch.setCreatedAt(LocalDateTime.now());
        batchMapper.insert(batch);
        return getBatch(batch.getId());
    }

    @Transactional
    public Map<String, Object> updateBatch(BatchCatalogDTOs.BatchUpdate dto, PermissionService.Actor actor) {
        HerbBatch batch = requireBatch(dto.id);
        assertOwner(actor, batch.getResponsiblePerson(), "药材批次");
        requireHerb(dto.herbId);
        assertUniqueBatchCode(dto.batchCode, dto.id);
        copyBatch(dto, batch);
        if (permissionService.isAdmin(actor) && dto.responsiblePerson != null && !dto.responsiblePerson.isBlank()) {
            batch.setResponsiblePerson(dto.responsiblePerson.trim());
        }
        batch.setCurrentStage(defaultValue(dto.currentStage, batch.getCurrentStage()));
        if (permissionService.isAdmin(actor) && dto.status != null && !dto.status.isBlank()) {
            batch.setStatus(dto.status.trim());
        }
        batch.setVersion(dto.version);
        if (batchMapper.update(batch) == 0) {
            throw new StateConflictException("药材批次已被其他用户修改，请刷新后重试");
        }
        return getBatch(batch.getId());
    }

    @Transactional
    public void deleteBatch(String id, PermissionService.Actor actor) {
        HerbBatch batch = requireBatch(id);
        assertOwner(actor, batch.getResponsiblePerson(), "药材批次");
        if (batchMapper.countReferences(id) > 0) {
            throw new StateConflictException("该批次已有生长、溯源、样本、分析或评价数据，不能删除，可改为归档状态");
        }
        batchMapper.deleteById(id);
    }

    @Transactional
    public Map<String, Object> createSample(BatchCatalogDTOs.SampleCreate dto, PermissionService.Actor actor) {
        requireBatch(dto.batchId);
        assertUniqueSampleCode(dto.sampleCode, null);
        LabSample sample = new LabSample();
        sample.setId(UUID.randomUUID().toString());
        copySample(dto, sample);
        sample.setCollectorName(valueOrActor(dto.collector, actor));
        sample.setStatus(defaultValue(dto.status, "collected"));
        sample.setVersion(0);
        sample.setCreatedAt(LocalDateTime.now());
        sampleMapper.insert(sample);
        return getSample(sample.getId());
    }

    @Transactional
    public Map<String, Object> updateSample(BatchCatalogDTOs.SampleUpdate dto, PermissionService.Actor actor) {
        LabSample sample = requireSample(dto.id);
        assertOwner(actor, sample.getCollectorName(), "检测样本");
        requireBatch(dto.batchId);
        assertUniqueSampleCode(dto.sampleCode, dto.id);
        copySample(dto, sample);
        if (permissionService.isAdmin(actor) && dto.collector != null && !dto.collector.isBlank()) {
            sample.setCollectorName(dto.collector.trim());
        }
        sample.setStatus(defaultValue(dto.status, sample.getStatus()));
        sample.setVersion(dto.version);
        if (sampleMapper.update(sample) == 0) {
            throw new StateConflictException("检测样本已被其他用户修改，请刷新后重试");
        }
        return getSample(sample.getId());
    }

    @Transactional
    public void deleteSample(String id, PermissionService.Actor actor) {
        LabSample sample = requireSample(id);
        assertOwner(actor, sample.getCollectorName(), "检测样本");
        if (sampleMapper.countReferences(id) > 0) {
            throw new StateConflictException("该样本已有图谱比对结果，不能删除");
        }
        sampleMapper.deleteById(id);
    }

    public void applyBatchContext(Map<String, Object> payload) {
        String batchId = text(payload.get("batchId"));
        if (batchId.isBlank()) return;
        HerbBatch batch = requireBatch(batchId);
        Herb herb = requireHerb(batch.getHerbId());
        payload.put("batchId", batch.getId());
        payload.put("herbName", herb.getName());
        payload.put("district", batch.getDistrict());
        if (payload.containsKey("traceCode")) payload.put("traceCode", batch.getTraceCode());
    }

    public void applySampleContext(Map<String, Object> payload) {
        String sampleId = text(payload.get("sampleId"));
        if (sampleId.isBlank()) {
            applyBatchContext(payload);
            return;
        }
        LabSample sample = requireSample(sampleId);
        payload.put("sampleId", sample.getId());
        payload.put("sampleCode", sample.getSampleCode());
        payload.put("batchId", sample.getBatchId());
        applyBatchContext(payload);
    }

    private void copyBatch(BatchCatalogDTOs.BatchCreate dto, HerbBatch batch) {
        batch.setHerbId(dto.herbId);
        batch.setBatchCode(dto.batchCode.trim());
        batch.setBatchName(dto.batchName.trim());
        batch.setTraceCode(trim(dto.traceCode));
        batch.setPlotName(trim(dto.plotName));
        batch.setDistrict(trim(dto.district));
        batch.setLongitude(dto.longitude);
        batch.setLatitude(dto.latitude);
        batch.setScaleDesc(trim(dto.scale));
        batch.setEnvironment(trim(dto.environment));
        batch.setPlantingDate(dto.plantingDate);
        batch.setExpectedHarvestDate(dto.expectedHarvestDate);
    }

    private void copySample(BatchCatalogDTOs.SampleCreate dto, LabSample sample) {
        sample.setBatchId(dto.batchId);
        sample.setSampleCode(dto.sampleCode.trim());
        sample.setSampleType(trim(dto.sampleType));
        sample.setCollectedAt(dto.getCollectedAt());
        sample.setSampleLocation(trim(dto.sampleLocation));
        sample.setStorageCondition(trim(dto.storageCondition));
    }

    private Herb requireHerb(String id) {
        Herb herb = herbMapper.findById(id);
        if (herb == null) throw new IllegalArgumentException("关联的药材档案不存在");
        return herb;
    }

    private HerbBatch requireBatch(String id) {
        HerbBatch batch = batchMapper.findById(id);
        if (batch == null) throw new IllegalArgumentException("关联的药材批次不存在");
        return batch;
    }

    private LabSample requireSample(String id) {
        LabSample sample = sampleMapper.findById(id);
        if (sample == null) throw new IllegalArgumentException("关联的检测样本不存在");
        return sample;
    }

    private void assertUniqueBatchCode(String code, String currentId) {
        HerbBatch duplicate = batchMapper.findByBatchCode(code.trim());
        if (duplicate != null && !duplicate.getId().equals(currentId)) {
            throw new IllegalArgumentException("批次编号已存在");
        }
    }

    private void assertUniqueSampleCode(String code, String currentId) {
        LabSample duplicate = sampleMapper.findBySampleCode(code.trim());
        if (duplicate != null && !duplicate.getId().equals(currentId)) {
            throw new IllegalArgumentException("样本编号已存在");
        }
    }

    private void assertOwner(PermissionService.Actor actor, String owner, String label) {
        if (permissionService.isAdmin(actor)) return;
        if (!permissionService.isTeacher(actor) && !permissionService.isResearcher(actor)) {
            throw new AuthorizationDeniedException("仅教师和科研人员可维护" + label);
        }
        if (!actor.name().equals(owner)) {
            throw new AuthorizationDeniedException("仅负责人可以修改或删除该" + label);
        }
    }

    private String valueOrActor(String value, PermissionService.Actor actor) {
        if (permissionService.isAdmin(actor) && value != null && !value.isBlank()) return value.trim();
        return actor.name();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private List<Map<String, Object>> visibleWorkflow(List<Map<String, Object>> records,
                                                       PermissionService.Actor actor) {
        if (!permissionService.isStudent(actor)) return records;
        return records.stream()
                .filter(item -> StatusMachine.isStudentVisible(text(item.get("status"))))
                .toList();
    }
}
