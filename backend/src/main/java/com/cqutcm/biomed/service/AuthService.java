package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final Duration SESSION_TTL = Duration.ofHours(8);

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(Map<String, Object> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        String password = String.valueOf(payload.getOrDefault("password", ""));
        DemoUser user = findUser(username);
        if (user == null || !passwordEncoder.matches(password, user.password())) {
            throw new AuthenticationRequiredException("username or password is incorrect");
        }
        removeExpiredSessions();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(SESSION_TTL);
        sessions.put(token, new UserSession(user, expiresAt));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("expiresAt", expiresAt.toString());
        result.put("username", user.username());
        result.put("role", user.role());
        result.put("name", user.name());
        result.put("roleLabel", user.roleLabel());
        return result;
    }

    private DemoUser findUser(String username) {
        SysUser sysUser = sysUserMapper.findByUsername(username);
        if (sysUser != null && "enabled".equals(sysUser.getStatus())
                && sysUser.getPasswordHash() != null && !sysUser.getPasswordHash().isBlank()) {
            String roleCode = "student";
            String roleLabel = "学生";
            List<Map<String, Object>> roles = sysUserMapper.findRolesByUserId(sysUser.getId());
            if (!roles.isEmpty()) {
                roleCode = String.valueOf(roles.get(0).getOrDefault("code", "student"));
                roleLabel = String.valueOf(roles.get(0).getOrDefault("name", "学生"));
            }
            return new DemoUser(sysUser.getUsername(), sysUser.getPasswordHash(),
                    roleCode, sysUser.getDisplayName(), roleLabelCode(roleCode, roleLabel));
        }
        return null;
    }

    public PermissionService.Actor requireActor(String authorization) {
        String token = extractToken(authorization);
        UserSession session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            throw new AuthenticationRequiredException("login required");
        }
        DemoUser user = session.user();
        return new PermissionService.Actor(user.name(), user.role());
    }

    public void logout(String authorization) {
        String token = extractToken(authorization);
        if (!token.isBlank()) {
            sessions.remove(token);
        }
    }

    private void removeExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) return "";
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return authorization.trim();
    }

    private String roleLabelCode(String role, String fallback) {
        return switch (role == null ? "" : role) {
            case "admin" -> "管理员";
            case "teacher" -> "教师";
            case "researcher" -> "科研人员";
            case "student" -> "学生";
            default -> fallback == null || fallback.isBlank() ? role : fallback;
        };
    }

    private record DemoUser(String username, String password, String role, String name, String roleLabel) {}
    private record UserSession(DemoUser user, Instant expiresAt) {}
}
