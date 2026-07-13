package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request
    ) {
        Map<String, Object> result = authService.login(payload);
        ResponseCookie cookie = sessionCookie(String.valueOf(result.get("token")), request.isSecure(), authService.getSessionTtl());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String oldPassword = payload.getOrDefault("oldPassword", "");
        String newPassword = payload.getOrDefault("newPassword", "");
        authService.changePassword(authorization, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie,
            HttpServletRequest request
    ) {
        authService.logout(authorization, sessionCookie);
        ResponseCookie expired = sessionCookie("", request.isSecure(), java.time.Duration.ZERO);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(Map.of("message", "logged out"));
    }

    private ResponseCookie sessionCookie(String value, boolean secure, java.time.Duration maxAge) {
        return ResponseCookie.from(AuthService.SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
