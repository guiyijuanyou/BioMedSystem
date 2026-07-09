package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.cqutcm.biomed.repository.GrowthRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GrowthRecordService {
    private static final String STUDENT = "\u5b66\u751f";
    private static final String CURRENT_STUDENT = "\u5f53\u524d\u5b66\u751f";

    private final GrowthRecordRepository growthRepository;
    private final GenericRecordRepository genericRepository;
    private final PermissionService permissionService;

    public GrowthRecordService(GrowthRecordRepository growthRepository, GenericRecordRepository genericRepository, PermissionService permissionService) {
        this.growthRepository = growthRepository;
        this.genericRepository = genericRepository;
        this.permissionService = permissionService;
    }

    public List<Map<String, Object>> list() {
        seedFromGenericIfEmpty();
        return growthRepository.findAll();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        applyActorForCreate(cleaned, payload);
        LocalDateTime now = LocalDateTime.now();
        growthRepository.insert(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for growth record update");
        }
        Map<String, Object> existing = growthRepository.findById(id);
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
        growthRepository.update(id, cleaned);
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id, String actorName, String actorRole) {
        seedFromGenericIfEmpty();
        Map<String, Object> existing = growthRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("growth record not found");
        }
        if (!permissionService.canDeleteOwnedRecord(existing, permissionService.actor(actorName, actorRole), "recorder")) {
            throw new IllegalArgumentException("current role cannot delete this growth record");
        }
        growthRepository.delete(id);
    }

    public long count() {
        seedFromGenericIfEmpty();
        return growthRepository.count();
    }

    private void seedFromGenericIfEmpty() {
        if (growthRepository.count() > 0) return;
        genericRepository.findByResourceType("growth-records").forEach(record -> {
            if (!growthRepository.exists(record.getId())) {
                Map<String, Object> payload = new LinkedHashMap<>(record.getPayload());
                payload.putIfAbsent("recorder", CURRENT_STUDENT);
                payload.putIfAbsent("recorderRole", STUDENT);
                growthRepository.insert(record.getId(), payload, record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt());
            }
        });
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
