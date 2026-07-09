package com.cqutcm.biomed.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Repository
public class StructuredRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public StructuredRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean supports(String resourceType) {
        return definition(resourceType) != null;
    }

    public long count(String resourceType) {
        if ("users".equals(resourceType)) {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
        }
        Definition definition = requireDefinition(resourceType);
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + definition.tableName(), Long.class);
    }

    public boolean exists(String resourceType, String id) {
        if ("users".equals(resourceType)) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE id = ?", Integer.class, id);
            return count != null && count > 0;
        }
        Definition definition = requireDefinition(resourceType);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + definition.tableName() + " WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> findAll(String resourceType) {
        if ("users".equals(resourceType)) {
            return findUsers();
        }
        Definition definition = requireDefinition(resourceType);
        return jdbcTemplate.query(selectSql(definition), (rs, rowNum) -> mapRow(definition, rs));
    }

    public void insert(String resourceType, String id, Map<String, Object> payload, LocalDateTime createdAt) {
        if ("users".equals(resourceType)) {
            insertUser(id, payload, createdAt);
            return;
        }
        Definition definition = requireDefinition(resourceType);
        StringJoiner columns = new StringJoiner(", ", "id, ", ", created_at");
        StringJoiner placeholders = new StringJoiner(", ", "?, ", ", ?");
        List<Object> values = new ArrayList<>();
        values.add(id);
        for (Field field : definition.fields()) {
            columns.add(field.column());
            placeholders.add("?");
            values.add(toSqlValue(payload.get(field.apiName()), field.type()));
        }
        values.add(Timestamp.valueOf(createdAt));
        jdbcTemplate.update("INSERT INTO " + definition.tableName() + " (" + columns + ") VALUES (" + placeholders + ")", values.toArray());
    }

    public int update(String resourceType, String id, Map<String, Object> payload) {
        if ("users".equals(resourceType)) {
            return updateUser(id, payload);
        }
        Definition definition = requireDefinition(resourceType);
        StringJoiner assignments = new StringJoiner(", ");
        List<Object> values = new ArrayList<>();
        for (Field field : definition.fields()) {
            assignments.add(field.column() + " = ?");
            values.add(toSqlValue(payload.get(field.apiName()), field.type()));
        }
        assignments.add("updated_at = ?");
        values.add(Timestamp.valueOf(LocalDateTime.now()));
        values.add(id);
        return jdbcTemplate.update("UPDATE " + definition.tableName() + " SET " + assignments + " WHERE id = ?", values.toArray());
    }

    public int delete(String resourceType, String id) {
        if ("users".equals(resourceType)) {
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", id);
            return jdbcTemplate.update("DELETE FROM sys_user WHERE id = ?", id);
        }
        Definition definition = requireDefinition(resourceType);
        return jdbcTemplate.update("DELETE FROM " + definition.tableName() + " WHERE id = ?", id);
    }

    private List<Map<String, Object>> findUsers() {
        return jdbcTemplate.query(
                """
                SELECT u.id, u.username, u.display_name, u.password_hash, u.department, u.status,
                       COALESCE(r.code, '') AS role_code, COALESCE(r.name, '') AS role_label,
                       u.created_at, u.updated_at
                FROM sys_user u
                LEFT JOIN sys_user_role ur ON ur.user_id = u.id
                LEFT JOIN sys_role r ON r.id = ur.role_id
                ORDER BY u.created_at DESC
                """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getString("id"));
                    row.put("username", rs.getString("username"));
                    row.put("password", rs.getString("password_hash"));
                    row.put("name", rs.getString("display_name"));
                    row.put("role", rs.getString("role_code"));
                    row.put("department", rs.getString("department"));
                    row.put("level", rs.getString("role_label"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", toText(rs.getTimestamp("created_at")));
                    row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
                    return row;
                }
        );
    }

    private void insertUser(String id, Map<String, Object> payload, LocalDateTime createdAt) {
        String username = text(payload.get("username"));
        String password = text(payload.get("password"));
        String name = text(payload.get("name"));
        String department = text(payload.get("department"));
        String status = text(payload.get("status")).isBlank() ? "enabled" : text(payload.get("status"));
        String role = normalizeRole(payload.get("role"));
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, username, name, password, department, status, Timestamp.valueOf(createdAt)
        );
        assignRole(id, role);
    }

    private int updateUser(String id, Map<String, Object> payload) {
        String username = text(payload.get("username"));
        String password = text(payload.get("password"));
        String name = text(payload.get("name"));
        String department = text(payload.get("department"));
        String status = text(payload.get("status")).isBlank() ? "enabled" : text(payload.get("status"));
        String role = normalizeRole(payload.get("role"));
        int count = jdbcTemplate.update(
                "UPDATE sys_user SET username = ?, display_name = ?, password_hash = ?, department = ?, status = ?, updated_at = ? WHERE id = ?",
                username, name, password, department, status, Timestamp.valueOf(LocalDateTime.now()), id
        );
        assignRole(id, role);
        return count;
    }

    private void assignRole(String userId, String role) {
        String roleId = "role-" + role;
        ensureRole(roleId, role);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
        jdbcTemplate.update(
                "INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES (?, ?, ?)",
                userId, roleId, Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private void ensureRole(String roleId, String role) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role WHERE id = ?", Integer.class, roleId);
        if (count != null && count > 0) return;
        jdbcTemplate.update(
                "INSERT INTO sys_role (id, code, name, description, created_at) VALUES (?, ?, ?, ?, ?)",
                roleId, role, roleLabel(role), "Created by user management", Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private String normalizeRole(Object rawRole) {
        String role = text(rawRole);
        return switch (role) {
            case "admin", "teacher", "researcher", "student" -> role;
            case "\u7ba1\u7406\u5458" -> "admin";
            case "\u6559\u5e08" -> "teacher";
            case "\u79d1\u7814\u4eba\u5458" -> "researcher";
            case "\u5b66\u751f" -> "student";
            default -> "student";
        };
    }

    private String roleLabel(String role) {
        return switch (role) {
            case "admin" -> "\u7ba1\u7406\u5458";
            case "teacher" -> "\u6559\u5e08";
            case "researcher" -> "\u79d1\u7814\u4eba\u5458";
            default -> "\u5b66\u751f";
        };
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String selectSql(Definition definition) {
        StringJoiner columns = new StringJoiner(", ", "SELECT id, ", ", created_at, updated_at FROM " + definition.tableName() + " ORDER BY created_at DESC");
        for (Field field : definition.fields()) {
            columns.add(field.column());
        }
        return columns.toString();
    }

    private Map<String, Object> mapRow(Definition definition, ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("id"));
        for (Field field : definition.fields()) {
            row.put(field.apiName(), toResponseValue(rs, field));
        }
        row.put("createdAt", toText(rs.getTimestamp("created_at")));
        row.put("updatedAt", toText(rs.getTimestamp("updated_at")));
        return row;
    }

    private Object toResponseValue(ResultSet rs, Field field) throws SQLException {
        if ("number".equals(field.type())) {
            Object value = rs.getObject(field.column());
            return value == null ? "" : String.valueOf(value);
        }
        if ("date".equals(field.type())) {
            Date value = rs.getDate(field.column());
            return value == null ? "" : value.toLocalDate().toString();
        }
        return rs.getString(field.column());
    }

    private Object toSqlValue(Object rawValue, String type) {
        String value = rawValue == null ? "" : String.valueOf(rawValue);
        if ("number".equals(type)) {
            try {
                return value.isBlank() ? null : Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        if ("date".equals(type)) {
            try {
                return value.isBlank() ? null : Date.valueOf(LocalDate.parse(value));
            } catch (RuntimeException ex) {
                return null;
            }
        }
        return value;
    }

    private String toText(Timestamp timestamp) {
        return timestamp == null ? "" : timestamp.toLocalDateTime().toString();
    }

    private Definition requireDefinition(String resourceType) {
        Definition definition = definition(resourceType);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported structured resource: " + resourceType);
        }
        return definition;
    }

    private Definition definition(String resourceType) {
        return switch (resourceType) {
            case "herbs" -> new Definition("herb", List.of(
                    new Field("name", "name", "text"),
                    new Field("district", "district", "text"),
                    new Field("longitude", "longitude", "number"),
                    new Field("latitude", "latitude", "number"),
                    new Field("scale", "scale_desc", "text"),
                    new Field("environment", "environment", "text"),
                    new Field("traceCode", "trace_code", "text")
            ));
            case "trainings" -> new Definition("training_material", List.of(
                    new Field("title", "title", "text"),
                    new Field("trainer", "trainer_name", "text"),
                    new Field("audience", "audience", "text"),
                    new Field("tracking", "tracking", "text")
            ));
            case "evaluations" -> new Definition("evaluation_record", List.of(
                    new Field("herbName", "herb_name", "text"),
                    new Field("indicator", "indicator", "text"),
                    new Field("score", "score", "number"),
                    new Field("result", "result", "text"),
                    new Field("applicationMaterial", "application_material", "text")
            ));
            case "achievements" -> new Definition("achievement_record", List.of(
                    new Field("title", "title", "text"),
                    new Field("owner", "owner_name", "text"),
                    new Field("category", "category", "text"),
                    new Field("level", "level_name", "text"),
                    new Field("status", "status", "text")
            ));
            case "standards" -> new Definition("achievement_standard", List.of(
                    new Field("name", "name", "text"),
                    new Field("category", "category", "text"),
                    new Field("levelRule", "level_rule", "text"),
                    new Field("effectiveDate", "effective_date", "date")
            ));
            case "users" -> new Definition("sys_user", List.of(
                    new Field("username", "username", "text"),
                    new Field("password", "password_hash", "text"),
                    new Field("name", "display_name", "text"),
                    new Field("department", "department", "text"),
                    new Field("status", "status", "text")
            ));
            default -> null;
        };
    }

    private record Definition(String tableName, List<Field> fields) {}

    private record Field(String apiName, String column, String type) {}
}
