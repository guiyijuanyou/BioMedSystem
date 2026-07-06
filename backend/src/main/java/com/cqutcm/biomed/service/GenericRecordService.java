package com.cqutcm.biomed.service;

import com.cqutcm.biomed.model.GenericRecord;
import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public GenericRecordService(GenericRecordRepository repository, ResourceRegistry resourceRegistry, ObjectMapper objectMapper) {
        this.repository = repository;
        this.resourceRegistry = resourceRegistry;
        this.objectMapper = objectMapper;
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
            throw new IllegalArgumentException("缺少 id，无法更新记录");
        }
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");

        GenericRecord record = new GenericRecord();
        record.setId(id);
        record.setResourceType(resourceType);
        record.setPayload(cleaned);
        int updated = repository.update(record);
        if (updated == 0) {
            throw new IllegalArgumentException("未找到要更新的数据");
        }
        Map<String, Object> response = new LinkedHashMap<>(cleaned);
        response.put("id", id);
        response.put("updatedAt", LocalDateTime.now().toString());
        return response;
    }

    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("herbCount", repository.countByResourceType("herbs"));
        result.put("growthRecordCount", repository.countByResourceType("growth-records"));
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
            Files.writeString(target, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup), StandardCharsets.UTF_8);
            return target;
        } catch (Exception ex) {
            throw new IllegalStateException("备份失败", ex);
        }
    }

    public List<Map<String, Object>> queryHerbs(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        return list("herbs").stream()
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
}

