package com.cqutcm.biomed.service;

import com.cqutcm.biomed.mapper.ResearchProjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectRecordService {
    private static final String PENDING_REVIEW = "待审核";

    private final ResearchProjectMapper projectMapper;

    public ProjectRecordService(ResearchProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public List<Map<String, Object>> list() {
        return projectMapper.findAllAsMap();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        projectMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for project update");
        }
        String actorRole = String.valueOf(payload.getOrDefault("_actorRole", "admin"));
        if (!"admin".equals(actorRole)) {
            var existing = projectMapper.findById(id);
            if (existing != null) {
                payload.put("status", existing.getStatus());
            }
        }
        Map<String, Object> cleaned = clean(payload);
        cleaned.put("id", id);
        projectMapper.updateMap(cleaned);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("missing id for project delete");
        }
        if (projectMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("project not found");
        }
    }

    public long count() {
        return projectMapper.count();
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.remove("_actorName");
        cleaned.remove("_actorRole");
        cleaned.putIfAbsent("status", PENDING_REVIEW);
        cleaned.putIfAbsent("applicantRequests", "");
        cleaned.putIfAbsent("approvedMembers", "");
        cleaned.putIfAbsent("rejectedApplicants", "");
        return cleaned;
    }
}
