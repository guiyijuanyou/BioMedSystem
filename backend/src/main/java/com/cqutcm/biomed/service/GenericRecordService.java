package com.cqutcm.biomed.service;

import com.cqutcm.biomed.model.GenericRecord;
import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GenericRecordService {
    private final GenericRecordRepository repository;
    private final ResourceRegistry resourceRegistry;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public GenericRecordService(GenericRecordRepository repository, ResourceRegistry resourceRegistry, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.resourceRegistry = resourceRegistry;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list(String resourceType) {
        resourceRegistry.requireSupported(resourceType);
        return repository.findByResourceType(resourceType).stream().map(this::toResponse).toList();
    }

    public Map<String, Object> create(String resourceType, Map<String, Object> payload) {
        resourceRegistry.requireSupported(resourceType);
        GenericRecord record = new GenericRecord();
        record.setId(UUID.randomUUID().toString());
        record.setResourceType(resourceType);
        record.setCreatedAt(LocalDateTime.now());
        record.setPayload(new LinkedHashMap<>(payload));
        repository.insert(record);
        return toResponse(record);
    }

    public Map<String, Object> update(String resourceType, Map<String, Object> payload) {
        resourceRegistry.requireSupported(resourceType);
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for record update");
        }
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");

        GenericRecord record = new GenericRecord();
        record.setId(id);
        record.setResourceType(resourceType);
        record.setPayload(cleaned);
        if (repository.update(record) == 0) {
            throw new IllegalArgumentException("record not found for update");
        }
        Map<String, Object> response = new LinkedHashMap<>(cleaned);
        response.put("id", id);
        response.put("updatedAt", LocalDateTime.now().toString());
        return response;
    }

    public void delete(String resourceType, String id) {
        resourceRegistry.requireSupported(resourceType);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("missing id for record delete");
        }
        if (repository.delete(resourceType, id) == 0) {
            throw new IllegalArgumentException("record not found for delete");
        }
    }

    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("herbCount", repository.countByResourceType("herbs"));
        result.put("growthRecordCount", repository.countByResourceType("growth-records"));
        result.put("traceEventCount", repository.countByResourceType("trace-events"));
        result.put("teachingResourceCount", repository.countByResourceType("teaching-resources"));
        result.put("spectrumComparisonCount", repository.countByResourceType("spectrum-comparisons"));
        result.put("growthAnalysisCount", repository.countByResourceType("growth-analysis"));
        result.put("courseCount", repository.countByResourceType("courses"));
        result.put("projectCount", repository.countByResourceType("projects"));
        result.put("evaluationCount", repository.countByResourceType("evaluations"));
        result.put("achievementCount", repository.countByResourceType("achievements"));
        result.put("latestBackup", LocalDateTime.now().toString());
        return result;
    }

    public Path backup() {
        try {
            Files.createDirectories(Path.of("data"));
            Path target = Path.of("data", "backup-" + System.currentTimeMillis() + ".json");
            Map<String, Object> backup = new LinkedHashMap<>();
            for (GenericRecord record : repository.findAll()) {
                backup.computeIfAbsent(record.getResourceType(), key -> new java.util.ArrayList<Map<String, Object>>());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rows = (List<Map<String, Object>>) backup.get(record.getResourceType());
                rows.add(toResponse(record));
            }
            appendTable(backup, "growthRecords", "growth_record");
            appendTable(backup, "traceEvents", "trace_event");
            appendTable(backup, "spectrumComparisons", "spectrum_comparison");
            appendTable(backup, "growthAnalyses", "growth_analysis");
            appendTable(backup, "coursesNormalized", "course");
            appendTable(backup, "teachingResourcesNormalized", "teaching_resource");
            appendTable(backup, "researchProjects", "research_project");
            appendTable(backup, "projectApplications", "project_application");
            appendTable(backup, "projectMembers", "project_member");
            appendTable(backup, "herbsNormalized", "herb");
            appendTable(backup, "trainingsNormalized", "training_material");
            appendTable(backup, "evaluationsNormalized", "evaluation_record");
            appendTable(backup, "achievementsNormalized", "achievement_record");
            appendTable(backup, "standardsNormalized", "achievement_standard");
            appendTable(backup, "usersNormalized", "sys_user");
            appendTable(backup, "rolesNormalized", "sys_role");
            appendTable(backup, "userRolesNormalized", "sys_user_role");
            Files.writeString(target, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup), StandardCharsets.UTF_8);
            return target;
        } catch (Exception ex) {
            throw new IllegalStateException("backup failed", ex);
        }
    }

    public List<Map<String, Object>> queryHerbs(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at, updated_at FROM herb";
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getString("id"));
            row.put("name", rs.getString("name"));
            row.put("district", rs.getString("district"));
            row.put("longitude", rs.getString("longitude"));
            row.put("latitude", rs.getString("latitude"));
            row.put("scale", rs.getString("scale_desc"));
            row.put("environment", rs.getString("environment"));
            row.put("traceCode", rs.getString("trace_code"));
            row.put("createdAt", rs.getTimestamp("created_at") == null ? "" : rs.getTimestamp("created_at").toLocalDateTime().toString());
            row.put("updatedAt", rs.getTimestamp("updated_at") == null ? "" : rs.getTimestamp("updated_at").toLocalDateTime().toString());
            return row;
        });
        return rows.stream()
                .filter(item -> safeKeyword.isBlank()
                        || String.valueOf(item.getOrDefault("name", "")).contains(safeKeyword)
                        || String.valueOf(item.getOrDefault("district", "")).contains(safeKeyword))
                .toList();
    }

    private Map<String, Object> toResponse(GenericRecord record) {
        Map<String, Object> response = new LinkedHashMap<>(record.getPayload());
        response.put("id", record.getId());
        response.put("createdAt", record.getCreatedAt() == null ? "" : record.getCreatedAt().toString());
        if (record.getUpdatedAt() != null) {
            response.put("updatedAt", record.getUpdatedAt().toString());
        }
        return response;
    }

    private void appendTable(Map<String, Object> backup, String key, String tableName) {
        backup.put(key, jdbcTemplate.queryForList("SELECT * FROM " + tableName));
    }
}
