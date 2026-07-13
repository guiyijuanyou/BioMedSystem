package com.cqutcm.biomed.config;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.IntegrationAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Value("${app.cors.origins}")
    private String corsOrigins;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthService authService,
                                           IntegrationAuditService integrationAuditService,
                                           StringRedisTemplate redisTemplate) throws Exception {
        http
            // 无状态，不使用 HttpSession
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 禁用 CSRF（无状态 API + SameSite Cookie 已覆盖）
            .csrf(AbstractHttpConfigurer::disable)
            // CORS 配置
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 权限规则
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/map/tiles/**").permitAll()
                .requestMatchers("/api/mobile/growth-records/batch", "/api/mobile/sync/status").permitAll()
                .requestMatchers("/api/soap/**").permitAll()
                .requestMatchers("/api/actuator/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            // 自定义 JSON 格式的 401 / 403 响应
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(this::writeJsonError)
                .accessDeniedHandler(this::writeJsonError)
            )
            // 注册自定义 Token 认证过滤器
            .addFilterBefore(
                new TokenAuthenticationFilter(authService),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterAfter(
                new ExternalAuditFilter(integrationAuditService),
                TokenAuthenticationFilter.class
            )
            .addFilterAfter(
                new RateLimitFilter(redisTemplate),
                ExternalAuditFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /** 统一的 JSON 错误响应 */
    private void writeJsonError(HttpServletRequest request, HttpServletResponse response,
                                Exception ex) throws IOException {
        HttpStatus status;
        String code;
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            code = "AUTH_REQUIRED";
        } else {
            status = HttpStatus.FORBIDDEN;
            code = "ACCESS_DENIED";
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("status", status.value());
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
