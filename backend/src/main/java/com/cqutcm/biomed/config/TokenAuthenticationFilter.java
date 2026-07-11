package com.cqutcm.biomed.config;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.PermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一 Token 认证过滤器，从 Authorization header 或 BIOMED_SESSION cookie 提取 token。
 * 与 AuthService 配合，认证后的 Actor 存入 SecurityContextHolder。
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public TokenAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        String cookieToken = extractCookie(request, AuthService.SESSION_COOKIE);

        try {
            authService.requireActor(authorization, cookieToken);
        } catch (Exception e) {
            // 认证失败不在这里阻断，交给 Spring Security 处理
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
