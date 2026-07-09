package com.cqutcm.biomed.repository;

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
public class TraceEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public TraceEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trace_event", Long.class);
    }

    public boolean exists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trace_event WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.query(
                "SELECT id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at, updated_at " +
                        "FROM trace_event ORDER BY COALESCE(event_time, created_at) DESC",
                this::mapRow
        );
    }

    public void insert(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "herbName"),
                text(payload, "traceCode"),
                text(payload, "eventType"),
                text(payload, "eventContent"),
                text(payload, "operator"),
                timestampFromText(text(payload, "eventTime")),
                text(payload, "location"),
                Timestamp.valueOf(createdAt)
        );
    }

    public int update(String id, Map<String, Object> payload) {
        return jdbcTemplate.update(
                "UPDATE trace_event SET herb_name = ?, trace_code = ?, event_type = ?, event_content = ?, operator_name = ?, event_time = ?, location = ?, updated_at = ? WHERE id = ?",
                text(payload, "herbName"),
                text(payload, "traceCode"),
                text(payload, "eventType"),
                text(payload, "eventContent"),
                text(payload, "operator"),
                timestampFromText(text(payload, "eventTime")),
                text(payload, "location"),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM trace_event WHERE id = ?", id);
    }

    private Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("herbName", rs.getString("herb_name"));
        row.put("traceCode", rs.getString("trace_code"));
        row.put("eventType", rs.getString("event_type"));
        row.put("eventContent", rs.getString("event_content"));
        row.put("operator", rs.getString("operator_name"));
        row.put("eventTime", toText(rs.getTimestamp("event_time")));
        row.put("location", rs.getString("location"));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private Timestamp timestampFromText(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Timestamp.valueOf(value.replace("T", " "));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String toText(Timestamp timestamp) {
        return timestamp == null ? "" : timestamp.toLocalDateTime().toString();
    }
}
