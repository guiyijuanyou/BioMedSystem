package com.cqutcm.biomed.repository;

import com.cqutcm.biomed.model.GenericRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GenericRecordRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GenericRecordRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<GenericRecord> findByResourceType(String resourceType) {
        return jdbcTemplate.query(
                "SELECT id, resource_type, payload, created_at, updated_at FROM generic_record WHERE resource_type = ? ORDER BY created_at DESC",
                this::mapRow,
                resourceType
        );
    }

    public long countByResourceType(String resourceType) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM generic_record WHERE resource_type = ?",
                Long.class,
                resourceType
        );
    }

    public void insert(GenericRecord record) {
        jdbcTemplate.update(
                "INSERT INTO generic_record (id, resource_type, payload, created_at) VALUES (?, ?, ?, ?)",
                record.getId(),
                record.getResourceType(),
                writePayload(record.getPayload()),
                Timestamp.valueOf(record.getCreatedAt())
        );
    }

    public int update(GenericRecord record) {
        return jdbcTemplate.update(
                "UPDATE generic_record SET payload = ?, updated_at = ? WHERE id = ? AND resource_type = ?",
                writePayload(record.getPayload()),
                Timestamp.valueOf(LocalDateTime.now()),
                record.getId(),
                record.getResourceType()
        );
    }

    public int delete(String resourceType, String id) {
        return jdbcTemplate.update(
                "DELETE FROM generic_record WHERE id = ? AND resource_type = ?",
                id,
                resourceType
        );
    }

    public List<GenericRecord> findAll() {
        return jdbcTemplate.query(
                "SELECT id, resource_type, payload, created_at, updated_at FROM generic_record ORDER BY resource_type, created_at DESC",
                this::mapRow
        );
    }

    private GenericRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        GenericRecord record = new GenericRecord();
        record.setId(rs.getString("id"));
        record.setResourceType(rs.getString("resource_type"));
        record.setPayload(readPayload(rs.getString("payload")));
        record.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        record.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return record;
    }

    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("_invalidPayload", true);
            fallback.put("_rawPayload", payload == null ? "" : payload);
            return fallback;
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to write business JSON", ex);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
