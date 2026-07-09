package com.cqutcm.biomed.service;

import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final Map<String, DemoUser> USERS = Map.of(
            "admin", new DemoUser("admin", "123456", "admin", "\u7cfb\u7edf\u7ba1\u7406\u5458", "\u7ba1\u7406\u5458"),
            "teacher", new DemoUser("teacher", "123456", "teacher", "\u674e\u8001\u5e08", "\u6559\u5e08"),
            "researcher", new DemoUser("researcher", "123456", "researcher", "\u738b\u8001\u5e08", "\u79d1\u7814\u4eba\u5458"),
            "student", new DemoUser("student", "123456", "student", "\u5f53\u524d\u5b66\u751f", "\u5b66\u751f")
    );
    private final Map<String, DemoUser> sessions = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> login(Map<String, Object> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        String password = String.valueOf(payload.getOrDefault("password", ""));
        DemoUser user = findUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new IllegalArgumentException("username or password is incorrect");
        }
        String token = username + ":" + UUID.randomUUID();
        sessions.put(token, user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("username", user.username());
        result.put("role", user.role());
        result.put("name", user.name());
        result.put("roleLabel", user.roleLabel());
        return result;
    }

    private DemoUser findUser(String username) {
        try {
            List<DemoUser> users = jdbcTemplate.query(
                    """
                    SELECT u.username, u.password_hash, u.display_name, COALESCE(r.code, 'student') AS role_code, COALESCE(r.name, '\u5b66\u751f') AS role_label
                    FROM sys_user u
                    LEFT JOIN sys_user_role ur ON ur.user_id = u.id
                    LEFT JOIN sys_role r ON r.id = ur.role_id
                    WHERE u.username = ? AND u.status = 'enabled'
                    ORDER BY CASE r.code WHEN 'admin' THEN 1 WHEN 'teacher' THEN 2 WHEN 'researcher' THEN 3 WHEN 'student' THEN 4 ELSE 5 END
                    LIMIT 1
                    """,
                    (rs, rowNum) -> new DemoUser(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role_code"),
                            rs.getString("display_name"),
                            roleLabel(rs.getString("role_code"), rs.getString("role_label"))
                    ),
                    username
            );
            if (!users.isEmpty()) return users.get(0);
        } catch (RuntimeException ignored) {
            // Keep built-in users available when upgrading an older local database.
        }
        return USERS.get(username);
    }

    public PermissionService.Actor requireActor(String authorization) {
        String token = extractToken(authorization);
        DemoUser user = sessions.get(token);
        if (user == null) {
            user = restoreUserFromToken(token);
        }
        if (user == null) {
            throw new IllegalArgumentException("login required");
        }
        return new PermissionService.Actor(user.name(), user.role());
    }

    private DemoUser restoreUserFromToken(String token) {
        int separator = token == null ? -1 : token.indexOf(':');
        if (separator <= 0) return null;
        String username = token.substring(0, separator);
        DemoUser user = findUser(username);
        if (user != null) {
            sessions.put(token, user);
        }
        return user;
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) return "";
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return authorization.trim();
    }

    private String roleLabel(String role, String fallback) {
        return switch (role == null ? "" : role) {
            case "admin" -> "\u7ba1\u7406\u5458";
            case "teacher" -> "\u6559\u5e08";
            case "researcher" -> "\u79d1\u7814\u4eba\u5458";
            case "student" -> "\u5b66\u751f";
            default -> fallback == null || fallback.isBlank() ? role : fallback;
        };
    }

    private record DemoUser(String username, String password, String role, String name, String roleLabel) {}
}
