package com.cqutcm.biomed.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final RateLimitRule LOGIN_RULE = new RateLimitRule(5, Duration.ofMinutes(1));
    private static final RateLimitRule API_RULE = new RateLimitRule(100, Duration.ofSeconds(1));

    private final StringRedisTemplate redisTemplate;
    private final ConcurrentMap<String, RateLimitEntry> localCounters;
    private volatile boolean redisAvailable = true;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCounters = new ConcurrentHashMap<>();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);
        boolean isLogin = path.equals("/api/auth/login");
        RateLimitRule rule = isLogin ? LOGIN_RULE : API_RULE;
        String key = "ratelimit:" + clientIp + ":" + (isLogin ? "login" : "api");

        if (isRateLimited(key, rule)) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(rule.window().getSeconds()));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"status\":429,\"message\":\"请求过于频繁，请稍后重试\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String key, RateLimitRule rule) {
        if (redisAvailable) {
            try {
                String redisKey = "biomed:" + key;
                Long count = redisTemplate.opsForValue().increment(redisKey);
                if (count != null && count == 1) {
                    redisTemplate.expire(redisKey, rule.window());
                }
                return count != null && count > rule.limit();
            } catch (Exception e) {
                redisAvailable = false;
                log.warn("Redis rate-limit unavailable, falling back to local", e);
            }
        }
        // In-memory fallback with simple sliding window
        long now = System.currentTimeMillis();
        RateLimitEntry entry = localCounters.computeIfAbsent(key, k -> new RateLimitEntry());
        synchronized (entry) {
            if (now - entry.windowStart > rule.window().toMillis()) {
                entry.windowStart = now;
                entry.count.set(1);
                return false;
            }
            int current = entry.count.incrementAndGet();
            return current > rule.limit();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitRule(int limit, Duration window) {}

    private static class RateLimitEntry {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }
}
