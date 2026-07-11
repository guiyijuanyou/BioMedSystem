package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public static final String SESSION_COOKIE = "BIOMED_SESSION";
    public static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final String REDIS_SESSION_PREFIX = "biomed:session:";
    private static final String REDIS_LOGIN_FAIL_PREFIX = "biomed:loginfail:";
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, UserSession> fallbackSessions;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private volatile boolean redisAvailable = true;

    public AuthService(SysUserMapper sysUserMapper,
                       PasswordEncoder passwordEncoder,
                       StringRedisTemplate redisTemplate,
                       ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.fallbackSessions = new ConcurrentHashMap<>();
    }

    // ==================== LOGIN ====================

    public Map<String, Object> login(Map<String, Object> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        String password = String.valueOf(payload.getOrDefault("password", ""));

        // 检查是否被锁定
        checkLoginLock(username);

        DemoUser user = findUser(username);
        if (user == null || !passwordEncoder.matches(password, user.password())) {
            recordLoginFailure(username);
            throw new AuthenticationRequiredException("username or password is incorrect");
        }

        // 登录成功，清除失败计数
        clearLoginFailures(username);

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(SESSION_TTL);
        UserSession session = new UserSession(user, expiresAt);
        storeSession(token, session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("expiresAt", expiresAt.toString());
        result.put("username", user.username());
        result.put("role", user.role());
        result.put("name", user.name());
        result.put("roleLabel", user.roleLabel());
        return result;
    }

    // ==================== ACTOR RESOLUTION ====================

    public PermissionService.Actor requireActor(String authorization) {
        return requireActor(authorization, null);
    }

    public PermissionService.Actor requireActor(String authorization, String sessionCookie) {
        // 优先从 SecurityContext 获取
        TokenAuthToken existing = (TokenAuthToken) SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            return existing.getActor();
        }

        String token = resolveToken(authorization, sessionCookie);
        UserSession session = getSession(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            deleteSession(token);
            throw new AuthenticationRequiredException("login required");
        }
        // 续期
        refreshSession(token, session);
        DemoUser user = session.user();
        PermissionService.Actor actor = new PermissionService.Actor(user.name(), user.role());
        // 设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new TokenAuthToken(token, actor));
        return actor;
    }

    // ==================== LOGOUT ====================

    public void logout(String authorization) {
        logout(authorization, null);
    }

    public void logout(String authorization, String sessionCookie) {
        String token = resolveToken(authorization, sessionCookie);
        if (!token.isBlank()) {
            deleteSession(token);
        }
        SecurityContextHolder.clearContext();
    }

    // ==================== SESSION STORAGE ====================

    private void storeSession(String token, UserSession session) {
        if (redisAvailable) {
            try {
                Duration ttl = Duration.between(Instant.now(), session.expiresAt());
                String json = objectMapper.writeValueAsString(session);
                redisTemplate.opsForValue().set(REDIS_SESSION_PREFIX + token, json, ttl.getSeconds(), TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable, falling back to in-memory sessions", e);
                redisAvailable = false;
            }
        }
        fallbackSessions.put(token, session);
    }

    private UserSession getSession(String token) {
        if (token == null || token.isBlank()) return null;
        if (redisAvailable) {
            try {
                String json = redisTemplate.opsForValue().get(REDIS_SESSION_PREFIX + token);
                if (json != null) {
                    return objectMapper.readValue(json, UserSession.class);
                }
                return null;
            } catch (Exception e) {
                log.warn("Redis read failed, switching to in-memory fallback", e);
                redisAvailable = false;
            }
        }
        removeExpiredFallbackSessions();
        return fallbackSessions.get(token);
    }

    private void refreshSession(String token, UserSession session) {
        if (redisAvailable) {
            try {
                Duration ttl = Duration.between(Instant.now(), session.expiresAt());
                if (ttl.getSeconds() > 0) {
                    redisTemplate.expire(REDIS_SESSION_PREFIX + token, ttl.getSeconds(), TimeUnit.SECONDS);
                }
                return;
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        // fallback: 内存中无需显式续期
    }

    private void deleteSession(String token) {
        if (redisAvailable) {
            try {
                redisTemplate.delete(REDIS_SESSION_PREFIX + token);
                return;
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        fallbackSessions.remove(token);
    }

    private void removeExpiredFallbackSessions() {
        Instant now = Instant.now();
        fallbackSessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    // ==================== LOGIN FAILURE RATE LIMITING ====================

    private void checkLoginLock(String username) {
        if (redisAvailable) {
            try {
                String key = REDIS_LOGIN_FAIL_PREFIX + username;
                String countStr = redisTemplate.opsForValue().get(key);
                int count = countStr != null ? Integer.parseInt(countStr) : 0;
                if (count >= MAX_LOGIN_FAILURES) {
                    throw new StateConflictException(
                            "账号已被锁定，请 " + LOGIN_LOCK_DURATION.toMinutes() + " 分钟后重试");
                }
                return;
            } catch (StateConflictException e) {
                throw e;
            } catch (Exception e) {
                // Redis down, skip rate limiting but don't block login
                log.warn("Login rate-limit check skipped, Redis unavailable", e);
                return;
            }
        }
        // fallback: 无 Redis 时跳过期限制
    }

    private void recordLoginFailure(String username) {
        if (redisAvailable) {
            try {
                String key = REDIS_LOGIN_FAIL_PREFIX + username;
                Long newCount = redisTemplate.opsForValue().increment(key);
                if (newCount != null && newCount == 1) {
                    redisTemplate.expire(key, LOGIN_LOCK_DURATION.getSeconds(), TimeUnit.SECONDS);
                }
                return;
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
    }

    private void clearLoginFailures(String username) {
        if (redisAvailable) {
            try {
                redisTemplate.delete(REDIS_LOGIN_FAIL_PREFIX + username);
                return;
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
    }

    // ==================== TOKEN RESOLUTION ====================

    private String resolveToken(String authorization, String sessionCookie) {
        String headerToken = extractToken(authorization);
        if (!headerToken.isBlank()) return headerToken;
        return sessionCookie == null ? "" : sessionCookie.trim();
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) return "";
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return authorization.trim();
    }

    // ==================== USER LOOKUP ====================

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

    private String roleLabelCode(String role, String fallback) {
        return switch (role == null ? "" : role) {
            case "admin" -> "管理员";
            case "teacher" -> "教师";
            case "researcher" -> "科研人员";
            case "student" -> "学生";
            default -> fallback == null || fallback.isBlank() ? role : fallback;
        };
    }

    // ==================== INNER TYPES ====================

    public record DemoUser(String username, String password, String role, String name, String roleLabel) {}

    public record UserSession(DemoUser user, Instant expiresAt) {
        // 无参构造供 Jackson 反序列化
        public UserSession() {
            this(null, null);
        }
    }

    /** 简单的 Authentication 实现，供 SecurityContext 和 TokenAuthenticationFilter 使用 */
    public static class TokenAuthToken implements org.springframework.security.core.Authentication {
        private final String token;
        private final PermissionService.Actor actor;
        private boolean authenticated = true;

        public TokenAuthToken(String token, PermissionService.Actor actor) {
            this.token = token;
            this.actor = actor;
        }

        public PermissionService.Actor getActor() { return actor; }

        @Override public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>
            getAuthorities() { return java.util.List.of(); }
        @Override public Object getCredentials() { return token; }
        @Override public Object getDetails() { return null; }
        @Override public Object getPrincipal() { return actor.name(); }
        @Override public boolean isAuthenticated() { return authenticated; }
        @Override public void setAuthenticated(boolean isAuthenticated) { this.authenticated = isAuthenticated; }
        @Override public String getName() { return actor.name(); }
    }
}
