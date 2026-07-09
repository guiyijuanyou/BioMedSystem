package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.cqutcm.biomed.repository.TraceEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TraceEventService {
    private final TraceEventRepository traceRepository;
    private final GenericRecordRepository genericRepository;

    public TraceEventService(TraceEventRepository traceRepository, GenericRecordRepository genericRepository) {
        this.traceRepository = traceRepository;
        this.genericRepository = genericRepository;
    }

    public List<Map<String, Object>> list() {
        seedFromGenericIfEmpty();
        return traceRepository.findAll();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        traceRepository.insert(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        seedFromGenericIfEmpty();
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for trace event update");
        }
        Map<String, Object> cleaned = clean(payload);
        if (traceRepository.update(id, cleaned) == 0) {
            throw new IllegalArgumentException("trace event not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id) {
        seedFromGenericIfEmpty();
        if (traceRepository.delete(id) == 0) {
            throw new IllegalArgumentException("trace event not found");
        }
    }

    public long count() {
        seedFromGenericIfEmpty();
        return traceRepository.count();
    }

    private void seedFromGenericIfEmpty() {
        if (traceRepository.count() > 0) return;
        genericRepository.findByResourceType("trace-events").forEach(record -> {
            if (!traceRepository.exists(record.getId())) {
                traceRepository.insert(record.getId(), record.getPayload(), record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt());
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
}
