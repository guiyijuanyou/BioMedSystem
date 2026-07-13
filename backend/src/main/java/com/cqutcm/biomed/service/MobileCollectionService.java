package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MobileCollectionService {
    private static final int MAX_BATCH_SIZE = 200;

    private final JdbcTemplate jdbc;
    private final PermissionService permissions;
    private final MobileRecordIngestionService ingestionService;
    private final SecureRandom random = new SecureRandom();

    public MobileCollectionService(
            JdbcTemplate jdbc,
            PermissionService permissions,
            MobileRecordIngestionService ingestionService
    ) {
        this.jdbc = jdbc;
        this.permissions = permissions;
        this.ingestionService = ingestionService;
    }

    public List<Map<String, Object>> devices(PermissionService.Actor actor) {
        requireAdmin(actor, "only admin can manage collection devices");
        return jdbc.queryForList("""
                SELECT d.id, d.device_code AS deviceCode, d.device_name AS deviceName,
                       d.owner_name AS owner, d.platform_name AS platform, d.status,
                       d.last_seen_at AS lastSeenAt, d.created_at AS createdAt,
                       (SELECT COUNT(*) FROM mobile_sync_record s
                         WHERE s.device_id = d.id AND s.sync_status = 'accepted') AS acceptedCount,
                       (SELECT COUNT(*) FROM mobile_sync_record s
                         WHERE s.device_id = d.id AND s.sync_status = 'rejected') AS rejectedCount
                  FROM mobile_collection_device d
                 ORDER BY d.created_at DESC
                """);
    }

    @Transactional
    public Map<String, Object> register(Map<String, Object> body, PermissionService.Actor actor) {
        requireAdmin(actor, "only admin can register devices");
        String code = required(body, "deviceCode");
        String name = required(body, "deviceName");
        String token = newToken();
        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO mobile_collection_device(
                    id, device_code, device_name, owner_name, platform_name, token_hash, created_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, code, name, optional(body, "owner"), optional(body, "platform"),
                hash(token), actor.name());
        return Map.of(
                "id", id,
                "deviceCode", code,
                "deviceName", name,
                "token", token,
                "warning", "token is shown only once"
        );
    }

    @Transactional
    public Map<String, Object> rotate(String id, PermissionService.Actor actor) {
        requireAdmin(actor, "only admin can rotate tokens");
        String token = newToken();
        int updated = jdbc.update("""
                UPDATE mobile_collection_device
                   SET token_hash = ?, token_rotated_at = NOW(), updated_at = NOW()
                 WHERE id = ?
                """, hash(token), id);
        if (updated == 0) {
            throw new IllegalArgumentException("device not found");
        }
        return Map.of("token", token, "warning", "token is shown only once");
    }

    @Transactional
    public void setStatus(String id, String status, PermissionService.Actor actor) {
        requireAdmin(actor, "only admin can change device status");
        if (!List.of("active", "disabled").contains(status)) {
            throw new IllegalArgumentException("invalid device status");
        }
        if (jdbc.update("""
                UPDATE mobile_collection_device
                   SET status = ?, updated_at = NOW()
                 WHERE id = ?
                """, status, id) == 0) {
            throw new IllegalArgumentException("device not found");
        }
    }

    public Map<String, Object> syncStatus(String token) {
        Map<String, Object> device = requireActiveDevice(token);
        return Map.of(
                "deviceCode", device.get("device_code"),
                "accepted", countSyncRecords(device.get("id"), "accepted"),
                "rejected", countSyncRecords(device.get("id"), "rejected"),
                "processing", countSyncRecords(device.get("id"), "processing"),
                "lastSeenAt", String.valueOf(device.get("last_seen_at"))
        );
    }

    public Map<String, Object> ingest(String token, List<Map<String, Object>> records) {
        Map<String, Object> device = requireActiveDevice(token);
        validateBatch(records);

        List<Map<String, Object>> results = new ArrayList<>();
        int accepted = 0;
        int duplicates = 0;
        int rejected = 0;
        int processing = 0;

        for (Map<String, Object> record : records) {
            String clientRecordId = text(record, "clientRecordId");
            if (clientRecordId.isBlank()) {
                results.add(result("", "rejected", "clientRecordId is required"));
                rejected++;
                continue;
            }

            MobileRecordIngestionService.Claim claim = ingestionService.claim(
                    String.valueOf(device.get("id")), clientRecordId);
            if (!claim.claimed()) {
                String status = claim.status();
                if ("accepted".equals(status)) {
                    results.add(result(clientRecordId, "duplicate", claim.detail()));
                    duplicates++;
                } else {
                    results.add(result(clientRecordId, "processing", "record is being processed"));
                    processing++;
                }
                continue;
            }

            try {
                String growthRecordId = ingestionService.accept(device, record, clientRecordId);
                results.add(result(clientRecordId, "accepted", growthRecordId));
                accepted++;
            } catch (RuntimeException ex) {
                String message = safeErrorMessage(ex);
                ingestionService.reject(String.valueOf(device.get("id")), clientRecordId, message);
                results.add(result(clientRecordId, "rejected", message));
                rejected++;
            }
        }

        ingestionService.touchDevice(String.valueOf(device.get("id")));
        return Map.of(
                "accepted", accepted,
                "duplicates", duplicates,
                "rejected", rejected,
                "processing", processing,
                "items", results
        );
    }

    private void validateBatch(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("records are required");
        }
        if (records.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("at most 200 records per request");
        }
    }

    private Map<String, Object> requireActiveDevice(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationRequiredException("device token required");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM mobile_collection_device WHERE token_hash = ?", hash(token.trim()));
        if (rows.isEmpty()) {
            throw new AuthenticationRequiredException("invalid device token");
        }
        Map<String, Object> device = rows.getFirst();
        if (!"active".equals(device.get("status"))) {
            throw new AuthorizationDeniedException("device is disabled");
        }
        return device;
    }

    private int countSyncRecords(Object deviceId, String status) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM mobile_sync_record
                 WHERE device_id = ? AND sync_status = ?
                """, Integer.class, deviceId, status);
        return count == null ? 0 : count;
    }

    private void requireAdmin(PermissionService.Actor actor, String message) {
        if (!permissions.isAdmin(actor)) {
            throw new AuthorizationDeniedException(message);
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String required(Map<String, Object> body, String key) {
        String value = text(body, key);
        if (value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private Object optional(Map<String, Object> body, String key) {
        String value = text(body, key);
        return value.isBlank() ? null : value;
    }

    private String text(Map<String, Object> body, String key) {
        return String.valueOf(body.getOrDefault(key, "")).trim();
    }

    private String safeErrorMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "record processing failed";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private Map<String, Object> result(String clientRecordId, String status, String detail) {
        return Map.of(
                "clientRecordId", clientRecordId,
                "status", status,
                "detail", detail == null ? "" : detail
        );
    }
}
