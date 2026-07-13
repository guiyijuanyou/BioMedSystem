package com.cqutcm.biomed.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MobileRecordIngestionService {
    private final JdbcTemplate jdbc;

    public MobileRecordIngestionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Claim claim(String deviceId, String clientRecordId) {
        Claim existing = findClaim(deviceId, clientRecordId);
        if (existing == null) {
            try {
                jdbc.update("""
                        INSERT INTO mobile_sync_record(
                            id, device_id, client_record_id, sync_status
                        ) VALUES (?, ?, ?, 'processing')
                        """, UUID.randomUUID().toString(), deviceId, clientRecordId);
                return Claim.acquired();
            } catch (DuplicateKeyException ignored) {
                existing = findClaim(deviceId, clientRecordId);
            }
        }

        if (existing == null) {
            throw new IllegalStateException("unable to determine sync record status");
        }
        if ("rejected".equals(existing.status()) && reclaimRejected(deviceId, clientRecordId)) {
            return Claim.acquired();
        }
        if ("processing".equals(existing.status()) && reclaimStale(deviceId, clientRecordId)) {
            return Claim.acquired();
        }
        return existing;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String accept(Map<String, Object> device, Map<String, Object> record, String clientRecordId) {
        String growthRecordId = insertGrowth(device, record, clientRecordId);
        int updated = jdbc.update("""
                UPDATE mobile_sync_record
                   SET growth_record_id = ?, sync_status = 'accepted', error_message = NULL
                 WHERE device_id = ? AND client_record_id = ? AND sync_status = 'processing'
                """, growthRecordId, device.get("id"), clientRecordId);
        if (updated != 1) {
            throw new StateConflictException("sync record is no longer available for processing");
        }
        return growthRecordId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reject(String deviceId, String clientRecordId, String message) {
        jdbc.update("""
                UPDATE mobile_sync_record
                   SET growth_record_id = NULL, sync_status = 'rejected', error_message = ?
                 WHERE device_id = ? AND client_record_id = ? AND sync_status = 'processing'
                """, message, deviceId, clientRecordId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void touchDevice(String deviceId) {
        jdbc.update("""
                UPDATE mobile_collection_device
                   SET last_seen_at = NOW(), updated_at = NOW()
                 WHERE id = ?
                """, deviceId);
    }

    private Claim findClaim(String deviceId, String clientRecordId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT sync_status, growth_record_id, error_message
                  FROM mobile_sync_record
                 WHERE device_id = ? AND client_record_id = ?
                """, deviceId, clientRecordId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.getFirst();
        String status = String.valueOf(row.get("sync_status"));
        Object detail = "accepted".equals(status) ? row.get("growth_record_id") : row.get("error_message");
        return new Claim(false, status, detail == null ? "" : String.valueOf(detail));
    }

    private boolean reclaimRejected(String deviceId, String clientRecordId) {
        return jdbc.update("""
                UPDATE mobile_sync_record
                   SET sync_status = 'processing', error_message = NULL, received_at = NOW()
                 WHERE device_id = ? AND client_record_id = ? AND sync_status = 'rejected'
                """, deviceId, clientRecordId) == 1;
    }

    private boolean reclaimStale(String deviceId, String clientRecordId) {
        return jdbc.update("""
                UPDATE mobile_sync_record
                   SET error_message = NULL, received_at = NOW()
                 WHERE device_id = ? AND client_record_id = ?
                   AND sync_status = 'processing'
                   AND received_at < DATE_SUB(NOW(), INTERVAL 15 MINUTE)
                """, deviceId, clientRecordId) == 1;
    }

    private String insertGrowth(Map<String, Object> device, Map<String, Object> record, String clientRecordId) {
        String batchId = required(record, "batchId");
        Map<String, Object> batch = requireActiveBatch(batchId);
        BigDecimal latitude = decimal(record, "latitude");
        BigDecimal longitude = decimal(record, "longitude");
        BigDecimal accuracy = decimal(record, "locationAccuracy");
        validateLocation(latitude, longitude, accuracy);
        LocalDateTime recordedAt = parseRecordedAt(required(record, "recordedAt"));

        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO growth_record(
                    id, batch_id, herb_name, district, temperature, humidity, soil_ph,
                    growth_stage, collect_source, recorder_name, recorder_role, recorded_at,
                    longitude, latitude, location_accuracy, device_id, client_record_id, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'mobile_app', ?, 'device', ?, ?, ?, ?, ?, ?, NOW())
                """, id, batchId, batch.get("herb_name"), batch.get("district"),
                decimal(record, "temperature"), decimal(record, "humidity"), decimal(record, "soilPh"),
                optional(record, "growthStage"), device.get("owner_name"), recordedAt,
                longitude, latitude, accuracy, device.get("id"), clientRecordId);
        return id;
    }

    private Map<String, Object> requireActiveBatch(String batchId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT b.id, h.name AS herb_name, b.district
                  FROM herb_batch b
                  JOIN herb h ON h.id = b.herb_id
                 WHERE b.id = ? AND b.status = 'active'
                """, batchId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("active batch not found");
        }
        return rows.getFirst();
    }

    private void validateLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("longitude and latitude must be provided together");
        }
        if (latitude != null && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new IllegalArgumentException("invalid latitude");
        }
        if (longitude != null && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("invalid longitude");
        }
        if (accuracy != null && accuracy.signum() < 0) {
            throw new IllegalArgumentException("locationAccuracy cannot be negative");
        }
    }

    private LocalDateTime parseRecordedAt(String value) {
        try {
            LocalDateTime recordedAt = LocalDateTime.parse(value);
            if (recordedAt.isAfter(LocalDateTime.now().plusMinutes(10))) {
                throw new IllegalArgumentException("recordedAt cannot be more than 10 minutes in the future");
            }
            return recordedAt;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("recordedAt must use ISO local date-time format");
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

    private BigDecimal decimal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(key + " must be numeric");
        }
    }

    public record Claim(boolean claimed, String status, String detail) {
        public static Claim acquired() {
            return new Claim(true, "processing", "");
        }
    }
}
