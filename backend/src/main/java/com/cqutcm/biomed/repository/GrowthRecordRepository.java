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
public class GrowthRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public GrowthRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM growth_record", Long.class);
    }

    public boolean exists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM growth_record WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.query(
                "SELECT id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at, updated_at " +
                        "FROM growth_record ORDER BY COALESCE(recorded_at, created_at) DESC",
                this::mapRow
        );
    }

    public Map<String, Object> findById(String id) {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at, updated_at " +
                        "FROM growth_record WHERE id = ?",
                this::mapRow,
                id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void insert(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "herbName"),
                text(payload, "district"),
                number(payload, "temperature"),
                number(payload, "humidity"),
                number(payload, "soilPh"),
                text(payload, "growthStage"),
                text(payload, "collector"),
                text(payload, "recorder"),
                text(payload, "recorderRole"),
                timestampFromText(text(payload, "recordedAt")),
                Timestamp.valueOf(createdAt)
        );
    }

    public int update(String id, Map<String, Object> payload) {
        return jdbcTemplate.update(
                "UPDATE growth_record SET herb_name = ?, district = ?, temperature = ?, humidity = ?, soil_ph = ?, growth_stage = ?, " +
                        "collect_source = ?, recorder_name = ?, recorder_role = ?, recorded_at = ?, updated_at = ? WHERE id = ?",
                text(payload, "herbName"),
                text(payload, "district"),
                number(payload, "temperature"),
                number(payload, "humidity"),
                number(payload, "soilPh"),
                text(payload, "growthStage"),
                text(payload, "collector"),
                text(payload, "recorder"),
                text(payload, "recorderRole"),
                timestampFromText(text(payload, "recordedAt")),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int delete(String id) {
        return jdbcTemplate.update("DELETE FROM growth_record WHERE id = ?", id);
    }

    private Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("herbName", rs.getString("herb_name"));
        row.put("district", rs.getString("district"));
        row.put("temperature", textNumber(rs, "temperature"));
        row.put("humidity", textNumber(rs, "humidity"));
        row.put("soilPh", textNumber(rs, "soil_ph"));
        row.put("growthStage", rs.getString("growth_stage"));
        row.put("collector", rs.getString("collect_source"));
        row.put("recorder", rs.getString("recorder_name"));
        row.put("recorderRole", rs.getString("recorder_role"));
        row.put("recordedAt", toText(rs.getTimestamp("recorded_at")));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private String textNumber(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value == null ? "" : String.valueOf(value);
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private Double number(Map<String, Object> payload, String key) {
        try {
            String value = text(payload, key);
            return value.isBlank() ? null : Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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
