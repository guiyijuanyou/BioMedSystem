package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.StructuredRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StructuredRecordService {
    private final StructuredRecordRepository structuredRepository;

    public StructuredRecordService(StructuredRecordRepository structuredRepository) {
        this.structuredRepository = structuredRepository;
    }

    public boolean supports(String resourceType) {
        return structuredRepository.supports(resourceType);
    }

    public List<Map<String, Object>> list(String resourceType) {
        return structuredRepository.findAll(resourceType);
    }

    public Map<String, Object> create(String resourceType, Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        structuredRepository.insert(resourceType, id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> update(String resourceType, Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for " + resourceType + " update");
        }
        Map<String, Object> cleaned = clean(payload);
        if (structuredRepository.update(resourceType, id, cleaned) == 0) {
            throw new IllegalArgumentException(resourceType + " record not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String resourceType, String id) {
        if (structuredRepository.delete(resourceType, id) == 0) {
            throw new IllegalArgumentException(resourceType + " record not found");
        }
    }

    public long count(String resourceType) {
        return structuredRepository.count(resourceType);
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.remove("_actorName");
        cleaned.remove("_actorRole");
        return cleaned;
    }
}
