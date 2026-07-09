package com.cqutcm.biomed.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CourseRecordRepository {
    private static final String PENDING_REVIEW = "\u5f85\u5ba1\u6838";

    private final JdbcTemplate jdbcTemplate;

    public CourseRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countCourses() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM course", Long.class);
    }

    public long countResources() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM teaching_resource", Long.class);
    }

    public List<Map<String, Object>> findCourses() {
        return jdbcTemplate.query(
                "SELECT id, title, teacher_name, hours, material_type, status, created_at, updated_at FROM course ORDER BY created_at DESC",
                this::mapCourse
        );
    }

    public Map<String, Object> findCourseById(String id) {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT id, title, teacher_name, hours, material_type, status, created_at, updated_at FROM course WHERE id = ?",
                this::mapCourse,
                id
        );
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    public List<Map<String, Object>> findResources() {
        ensureFileLinkColumn();
        return jdbcTemplate.query(
                "SELECT r.id, r.course_title, r.title, r.resource_type, r.file_id, f.file_name, r.video_url, r.file_url, " +
                        "r.uploader_name, r.uploader_role, r.status, r.review_comment, r.published_at, r.created_at, r.updated_at " +
                        "FROM teaching_resource r LEFT JOIN file_asset f ON f.id = r.file_id ORDER BY r.created_at DESC",
                this::mapResource
        );
    }

    public Map<String, Object> findResourceById(String id) {
        ensureFileLinkColumn();
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT r.id, r.course_title, r.title, r.resource_type, r.file_id, f.file_name, r.video_url, r.file_url, " +
                        "r.uploader_name, r.uploader_role, r.status, r.review_comment, r.published_at, r.created_at, r.updated_at " +
                        "FROM teaching_resource r LEFT JOIN file_asset f ON f.id = r.file_id WHERE r.id = ?",
                this::mapResource,
                id
        );
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    public boolean courseExists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM course WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean courseTitleExists(String title) {
        if (title == null || title.isBlank()) return false;
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM course WHERE title = ?", Integer.class, title);
        return count != null && count > 0;
    }

    public boolean resourceExists(String id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM teaching_resource WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void insertCourse(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id,
                text(payload, "title"),
                text(payload, "teacher"),
                number(payload, "hours"),
                text(payload, "materialType"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                Timestamp.valueOf(createdAt)
        );
    }

    public int updateCourse(String id, Map<String, Object> payload) {
        return jdbcTemplate.update(
                "UPDATE course SET title = ?, teacher_name = ?, hours = ?, material_type = ?, status = ?, updated_at = ? WHERE id = ?",
                text(payload, "title"),
                text(payload, "teacher"),
                number(payload, "hours"),
                text(payload, "materialType"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int deleteCourse(String id) {
        jdbcTemplate.update("UPDATE teaching_resource SET course_id = NULL WHERE course_id = ?", id);
        return jdbcTemplate.update("DELETE FROM course WHERE id = ?", id);
    }

    public void insertResource(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        ensureFileLinkColumn();
        jdbcTemplate.update(
                "INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                courseIdByTitle(text(payload, "courseTitle")),
                text(payload, "courseTitle"),
                text(payload, "title"),
                text(payload, "resourceType"),
                nullIfBlank(text(payload, "fileId")),
                text(payload, "videoUrl"),
                text(payload, "fileUrl"),
                text(payload, "uploader"),
                text(payload, "uploaderRole"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                text(payload, "reviewComment"),
                timestampFromText(text(payload, "publishedAt")),
                Timestamp.valueOf(createdAt)
        );
    }

    public int updateResource(String id, Map<String, Object> payload) {
        ensureFileLinkColumn();
        return jdbcTemplate.update(
                "UPDATE teaching_resource SET course_id = ?, course_title = ?, title = ?, resource_type = ?, file_id = ?, video_url = ?, file_url = ?, " +
                        "uploader_name = ?, uploader_role = ?, status = ?, review_comment = ?, published_at = ?, updated_at = ? WHERE id = ?",
                courseIdByTitle(text(payload, "courseTitle")),
                text(payload, "courseTitle"),
                text(payload, "title"),
                text(payload, "resourceType"),
                nullIfBlank(text(payload, "fileId")),
                text(payload, "videoUrl"),
                text(payload, "fileUrl"),
                text(payload, "uploader"),
                text(payload, "uploaderRole"),
                textOrDefault(payload, "status", PENDING_REVIEW),
                text(payload, "reviewComment"),
                timestampFromText(text(payload, "publishedAt")),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public int deleteResource(String id) {
        return jdbcTemplate.update("DELETE FROM teaching_resource WHERE id = ?", id);
    }

    private void ensureFileLinkColumn() {
        if (hasColumn("teaching_resource", "file_id")) return;
        jdbcTemplate.execute("ALTER TABLE teaching_resource ADD COLUMN file_id VARCHAR(64)");
    }

    private boolean hasColumn(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) (Connection connection) -> {
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
                if (columns.next()) return true;
            }
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
                return columns.next();
            }
        }));
    }

    private Map<String, Object> mapCourse(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("title", rs.getString("title"));
        row.put("teacher", rs.getString("teacher_name"));
        row.put("hours", rs.getString("hours"));
        row.put("materialType", rs.getString("material_type"));
        row.put("status", rs.getString("status"));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private Map<String, Object> mapResource(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        row.put("title", rs.getString("title"));
        row.put("resourceType", rs.getString("resource_type"));
        row.put("courseTitle", rs.getString("course_title"));
        row.put("uploader", rs.getString("uploader_name"));
        row.put("uploaderRole", rs.getString("uploader_role"));
        row.put("status", rs.getString("status"));
        row.put("reviewComment", rs.getString("review_comment"));
        row.put("fileId", rs.getString("file_id"));
        row.put("fileName", rs.getString("file_name"));
        row.put("videoUrl", rs.getString("video_url"));
        row.put("fileUrl", rs.getString("file_url"));
        String fileId = rs.getString("file_id");
        if (fileId != null && !fileId.isBlank()) {
            row.put("previewUrl", "/api/files/" + fileId + "/preview");
            row.put("downloadUrl", "/api/files/" + fileId + "/download");
        }
        row.put("publishedAt", toText(rs.getTimestamp("published_at")));
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private String courseIdByTitle(String title) {
        if (title == null || title.isBlank()) return null;
        List<String> ids = jdbcTemplate.query("SELECT id FROM course WHERE title = ?", (rs, rowNum) -> rs.getString(1), title);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String textOrDefault(Map<String, Object> payload, String key, String defaultValue) {
        String value = text(payload, key);
        return value.isBlank() ? defaultValue : value;
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
