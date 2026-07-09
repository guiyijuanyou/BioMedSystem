package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.cqutcm.biomed.repository.ProjectRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectRecordService {
    private static final String PENDING_REVIEW = "\u5f85\u5ba1\u6838";

    private final ProjectRecordRepository projectRepository;
    private final GenericRecordRepository genericRepository;

    public ProjectRecordService(ProjectRecordRepository projectRepository, GenericRecordRepository genericRepository) {
        this.projectRepository = projectRepository;
        this.genericRepository = genericRepository;
    }

    public List<Map<String, Object>> list() {
        seedFromGenericIfEmpty();
        return projectRepository.findAll();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        projectRepository.insert(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for project update");
        }
        Map<String, Object> cleaned = clean(payload);
        int updated = projectRepository.update(id, cleaned);
        if (updated == 0) {
            throw new IllegalArgumentException("project not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id) {
        seedFromGenericIfEmpty();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("missing id for project delete");
        }
        if (projectRepository.delete(id) == 0) {
            throw new IllegalArgumentException("project not found");
        }
    }

    public long count() {
        seedFromGenericIfEmpty();
        return projectRepository.count();
    }

    private void seedFromGenericIfEmpty() {
        if (projectRepository.count() > 0) return;
        genericRepository.findByResourceType("projects").forEach(record -> {
            if (!projectRepository.exists(record.getId())) {
                projectRepository.insert(record.getId(), record.getPayload(), record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt());
            }
        });
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.putIfAbsent("status", PENDING_REVIEW);
        cleaned.putIfAbsent("applicantRequests", "");
        cleaned.putIfAbsent("approvedMembers", "");
        cleaned.putIfAbsent("rejectedApplicants", "");
        return cleaned;
    }
}
