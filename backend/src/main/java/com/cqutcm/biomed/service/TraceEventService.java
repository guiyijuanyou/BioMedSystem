package com.cqutcm.biomed.service;

import com.cqutcm.biomed.mapper.TraceEventMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TraceEventService {
    private final TraceEventMapper traceMapper;

    public TraceEventService(TraceEventMapper traceMapper) {
        this.traceMapper = traceMapper;
    }

    public List<Map<String, Object>> list() {
        return traceMapper.findAllAsMap();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        traceMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for trace event update");
        }
        Map<String, Object> cleaned = clean(payload);
        cleaned.put("id", id);
        traceMapper.updateMap(cleaned);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id) {
        if (traceMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("trace event not found");
        }
    }

    public long count() {
        return traceMapper.count();
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
