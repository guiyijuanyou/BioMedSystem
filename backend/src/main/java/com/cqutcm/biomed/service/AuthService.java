package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final Map<String, DemoUser> sessions = new ConcurrentHashMap<>();
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
}
