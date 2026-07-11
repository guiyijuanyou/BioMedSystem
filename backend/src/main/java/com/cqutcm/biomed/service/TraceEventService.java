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
    private final PermissionService permissionService;
    private final BatchCatalogService batchCatalogService;

    public TraceEventService(TraceEventMapper traceMapper, PermissionService permissionService,
                             BatchCatalogService batchCatalogService) {
        this.traceMapper = traceMapper;
        this.permissionService = permissionService;
        this.batchCatalogService = batchCatalogService;
    }

    public List<Map<String, Object>> list() {
        return traceMapper.findAllAsMap();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        batchCatalogService.applyBatchContext(cleaned);
        // 操作人由登录会话强制确定
        PermissionService.Actor actor = permissionService.actor(payload);
        if (!permissionService.isAdmin(actor)) {
            cleaned.put("operatorName", actor.name());
        }
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
        Map<String, Object> existing = traceMapper.findByIdAsMap(id);
        if (existing == null) {
            throw new IllegalArgumentException("trace event not found");
        }
        // 非管理员仅可修改自身记录
        PermissionService.Actor actor = permissionService.actor(payload);
        if (!permissionService.isAdmin(actor)) {
            String operator = String.valueOf(existing.getOrDefault("operatorName", ""));
            if (!actor.name().equals(operator)) {
                throw new AuthorizationDeniedException("仅可修改自身创建的溯源事件");
            }
        }
        Map<String, Object> cleaned = clean(payload);
        cleaned.putIfAbsent("batchId", existing.get("batchId"));
        batchCatalogService.applyBatchContext(cleaned);
        if (!permissionService.isAdmin(actor)) {
            cleaned.put("operatorName", existing.get("operatorName"));
        }
        cleaned.put("id", id);
        cleaned.put("version", existing.getOrDefault("version", 0));
        if (traceMapper.updateMap(cleaned) == 0) {
            throw new StateConflictException("溯源事件已被其他用户修改，请刷新后重试");
        }
        cleaned.put("version", ((Number) cleaned.get("version")).intValue() + 1);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void delete(String id) {
        if (traceMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("trace event not found");
        }
    }

    public void delete(String id, PermissionService.Actor actor) {
        if (!permissionService.isAdmin(actor)) {
            Map<String, Object> existing = traceMapper.findByIdAsMap(id);
            if (existing == null) throw new IllegalArgumentException("trace event not found");
            String operator = String.valueOf(existing.getOrDefault("operatorName", ""));
            if (!actor.name().equals(operator)) {
                throw new AuthorizationDeniedException("仅可删除自身创建的溯源事件");
            }
        }
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
