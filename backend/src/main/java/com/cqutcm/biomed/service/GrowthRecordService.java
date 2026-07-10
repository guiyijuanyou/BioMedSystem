package com.cqutcm.biomed.service;

import com.cqutcm.biomed.mapper.GrowthRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GrowthRecordService {
    private static final String STUDENT = "学生";

    private final GrowthRecordMapper growthMapper;
    private final PermissionService permissionService;

    public GrowthRecordService(GrowthRecordMapper growthMapper, PermissionService permissionService) {
        this.growthMapper = growthMapper;
        this.permissionService = permissionService;
    }

    public List<Map<String, Object>> list() {
        return growthMapper.findAllAsMap();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        applyActorForCreate(cleaned, payload);
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        growthMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for growth record update");
        }
        Map<String, Object> existing = growthMapper.findByIdAsMap(id);
        if (existing == null) {
            throw new IllegalArgumentException("growth record not found");
        }
        PermissionService.Actor actor = permissionService.actor(payload);
        if (!permissionService.canEditOwnedRecord(existing, actor, "recorder", "recorderRole")) {
            throw new IllegalArgumentException("current role cannot edit this growth record");
        }

        Map<String, Object> cleaned = clean(payload);
        cleaned.put("recorder", existing.getOrDefault("recorder", ""));
        cleaned.put("recorderRole", existing.getOrDefault("recorderRole", ""));
        cleaned.put("id", id);
        growthMapper.updateMap(cleaned);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id, String actorName, String actorRole) {
        Map<String, Object> existing = growthMapper.findByIdAsMap(id);
        if (existing == null) {
            throw new IllegalArgumentException("growth record not found");
        }
        if (!permissionService.canDeleteOwnedRecord(existing, permissionService.actor(actorName, actorRole), "recorder")) {
            throw new IllegalArgumentException("current role cannot delete this growth record");
        }
        growthMapper.deleteById(id);
    }

    public long count() {
        return growthMapper.count();
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

    private void applyActorForCreate(Map<String, Object> cleaned, Map<String, Object> payload) {
        PermissionService.Actor actor = permissionService.actor(payload);
        if (!"admin".equals(actor.role())) {
            cleaned.put("recorder", actor.name());
            cleaned.put("recorderRole", permissionService.roleLabel(actor.role()));
        }
    }
}
