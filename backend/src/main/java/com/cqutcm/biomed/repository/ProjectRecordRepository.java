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
public class ProjectRecordRepository {
    private static final String PENDING_REVIEW = "\u5f85\u5ba1\u6838";
    private static final String PENDING_APPROVAL = "\u5f85\u5ba1\u6279";
    private static final String REJECTED = "\u5df2\u62d2\u7edd";
    private static final String STUDENT = "\u5b66\u751f";
    private static final String NAME_JOINER = "\u3001";

    private final JdbcTemplate jdbcTemplate;

    public ProjectRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM research_project", Long.class);
    }

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.query(
                "SELECT id, title, leader_name, requirements, status, stage, transformation, created_at, updated_at " +
                        "FROM research_project ORDER BY created_at DESC",
                this::mapProject
        );
    }

    public boolean exists(String id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM research_project WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    public void insert(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "title"),
                text(payload, "leader"),
                text(payload, "requirements"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                text(payload, "stage"),
                text(payload, "transformation"),
                Timestamp.valueOf(createdAt)
        );
        syncRelations(id, payload);
    }

    public int update(String id, Map<String, Object> payload) {
        int updated = jdbcTemplate.update(
                "UPDATE research_project SET title = ?, leader_name = ?, requirements = ?, status = ?, stage = ?, transformation = ?, updated_at = ? " +
                        "WHERE id = ?",
                text(payload, "title"),
                text(payload, "leader"),
                text(payload, "requirements"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                text(payload, "stage"),
                text(payload, "transformation"),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
        if (updated > 0) {
            syncRelations(id, payload);
        }
        return updated;
    }

    public int delete(String id) {
        jdbcTemplate.update("DELETE FROM project_application WHERE project_id = ?", id);
        jdbcTemplate.update("DELETE FROM project_member WHERE project_id = ?", id);
        return jdbcTemplate.update("DELETE FROM research_project WHERE id = ?", id);
    }

    private Map<String, Object> mapProject(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("id");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("title", rs.getString("title"));
        row.put("leader", rs.getString("leader_name"));
        row.put("requirements", rs.getString("requirements"));
        row.put("status", rs.getString("status"));
        row.put("stage", rs.getString("stage"));
        row.put("applicantRequests", names("SELECT student_name FROM project_application WHERE project_id = ? AND status = ? ORDER BY applied_at", id, PENDING_APPROVAL));
        row.put("approvedMembers", names("SELECT member_name FROM project_member WHERE project_id = ? ORDER BY joined_at", id));
        row.put("rejectedApplicants", names("SELECT student_name FROM project_application WHERE project_id = ? AND status = ? ORDER BY reviewed_at, applied_at", id, REJECTED));
        row.put("transformation", rs.getString("transformation"));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private void syncRelations(String projectId, Map<String, Object> payload) {
        jdbcTemplate.update("DELETE FROM project_application WHERE project_id = ?", projectId);
        jdbcTemplate.update("DELETE FROM project_member WHERE project_id = ?", projectId);
        for (String name : splitNames(text(payload, "applicantRequests"))) {
            insertApplication(projectId, name, PENDING_APPROVAL);
        }
        for (String name : splitNames(text(payload, "rejectedApplicants"))) {
            insertApplication(projectId, name, REJECTED);
        }
        for (String name : splitNames(text(payload, "approvedMembers"))) {
            jdbcTemplate.update(
                    "INSERT INTO project_member (id, project_id, member_name, member_role, joined_at) VALUES (?, ?, ?, ?, ?)",
                    relationId(projectId, "member", name),
                    projectId,
                    name,
                    STUDENT,
                    Timestamp.valueOf(LocalDateTime.now())
            );
        }
    }

    private void insertApplication(String projectId, String studentName, String status) {
        jdbcTemplate.update(
                "INSERT INTO project_application (id, project_id, student_name, status, applied_at, reviewed_at) VALUES (?, ?, ?, ?, ?, ?)",
                relationId(projectId, REJECTED.equals(status) ? "reject" : "apply", studentName),
                projectId,
                studentName,
                status,
                Timestamp.valueOf(LocalDateTime.now()),
                REJECTED.equals(status) ? Timestamp.valueOf(LocalDateTime.now()) : null
        );
    }

    private String names(String sql, String projectId) {
        return String.join(NAME_JOINER, jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), projectId));
    }

    private String names(String sql, String projectId, String status) {
        return String.join(NAME_JOINER, jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), projectId, status));
    }

    private List<String> splitNames(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split("[\u3001,;\uff0c\uff1b\\n]"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String textOrDefault(Map<String, Object> payload, String key, String defaultValue) {
        String value = text(payload, key);
        return value.isBlank() ? defaultValue : value;
    }

    private String relationId(String projectId, String type, String name) {
        return projectId + "-" + type + "-" + Integer.toHexString(name.hashCode());
    }

    private String toText(Timestamp timestamp) {
        return timestamp == null ? "" : timestamp.toLocalDateTime().toString();
    }
}
