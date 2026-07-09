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
public class ResearchDataRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResearchDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countSpectrum() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM spectrum_comparison", Long.class);
    }

    public long countAnalysis() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM growth_analysis", Long.class);
    }

    public boolean spectrumExists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM spectrum_comparison WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean analysisExists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM growth_analysis WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> findSpectrum() {
        return jdbcTemplate.query(
                "SELECT id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at, updated_at " +
                        "FROM spectrum_comparison ORDER BY COALESCE(compared_at, created_at) DESC",
                this::mapSpectrum
        );
    }

    public List<Map<String, Object>> findAnalysis() {
        return jdbcTemplate.query(
                "SELECT id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at, updated_at " +
                        "FROM growth_analysis ORDER BY COALESCE(analyzed_at, created_at) DESC",
                this::mapAnalysis
        );
    }

    public void insertSpectrum(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "herbName"),
                text(payload, "sampleCode"),
                text(payload, "district"),
                text(payload, "spectrumType"),
                text(payload, "referenceName"),
                number(payload, "similarity"),
                text(payload, "result"),
                text(payload, "operator"),
                timestampFromText(text(payload, "comparedAt")),
                text(payload, "remark"),
                Timestamp.valueOf(createdAt)
        );
    }

    public int updateSpectrum(String id, Map<String, Object> payload) {
        return jdbcTemplate.update(
                "UPDATE spectrum_comparison SET herb_name = ?, sample_code = ?, district = ?, spectrum_type = ?, reference_name = ?, similarity = ?, " +
                        "result = ?, operator_name = ?, compared_at = ?, remark = ?, updated_at = ? WHERE id = ?",
                text(payload, "herbName"),
                text(payload, "sampleCode"),
                text(payload, "district"),
                text(payload, "spectrumType"),
                text(payload, "referenceName"),
                number(payload, "similarity"),
                text(payload, "result"),
                text(payload, "operator"),
                timestampFromText(text(payload, "comparedAt")),
                text(payload, "remark"),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int deleteSpectrum(String id) {
        return jdbcTemplate.update("DELETE FROM spectrum_comparison WHERE id = ?", id);
    }

    public void insertAnalysis(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "analysisName"),
                text(payload, "herbName"),
                text(payload, "district"),
                text(payload, "indicator"),
                text(payload, "baseline"),
                text(payload, "currentValue"),
                text(payload, "difference"),
                text(payload, "trend"),
                text(payload, "conclusion"),
                text(payload, "analyst"),
                timestampFromText(text(payload, "analyzedAt")),
                Timestamp.valueOf(createdAt)
        );
    }

    public int updateAnalysis(String id, Map<String, Object> payload) {
        return jdbcTemplate.update(
                "UPDATE growth_analysis SET analysis_name = ?, herb_name = ?, district = ?, indicator = ?, baseline = ?, current_value = ?, difference_desc = ?, " +
                        "trend = ?, conclusion = ?, analyst_name = ?, analyzed_at = ?, updated_at = ? WHERE id = ?",
                text(payload, "analysisName"),
                text(payload, "herbName"),
                text(payload, "district"),
                text(payload, "indicator"),
                text(payload, "baseline"),
                text(payload, "currentValue"),
                text(payload, "difference"),
                text(payload, "trend"),
                text(payload, "conclusion"),
                text(payload, "analyst"),
                timestampFromText(text(payload, "analyzedAt")),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int deleteAnalysis(String id) {
        return jdbcTemplate.update("DELETE FROM growth_analysis WHERE id = ?", id);
    }

    private Map<String, Object> mapSpectrum(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("herbName", rs.getString("herb_name"));
        row.put("sampleCode", rs.getString("sample_code"));
        row.put("district", rs.getString("district"));
        row.put("spectrumType", rs.getString("spectrum_type"));
        row.put("referenceName", rs.getString("reference_name"));
        row.put("similarity", textNumber(rs, "similarity"));
        row.put("result", rs.getString("result"));
        row.put("operator", rs.getString("operator_name"));
        row.put("comparedAt", toText(rs.getTimestamp("compared_at")));
        row.put("remark", rs.getString("remark"));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private Map<String, Object> mapAnalysis(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("analysisName", rs.getString("analysis_name"));
        row.put("herbName", rs.getString("herb_name"));
        row.put("district", rs.getString("district"));
        row.put("indicator", rs.getString("indicator"));
        row.put("baseline", rs.getString("baseline"));
        row.put("currentValue", rs.getString("current_value"));
        row.put("difference", rs.getString("difference_desc"));
        row.put("trend", rs.getString("trend"));
        row.put("conclusion", rs.getString("conclusion"));
        row.put("analyst", rs.getString("analyst_name"));
        row.put("analyzedAt", toText(rs.getTimestamp("analyzed_at")));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
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

    private String textNumber(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
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
