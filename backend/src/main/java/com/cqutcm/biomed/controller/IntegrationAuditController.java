package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.IntegrationAuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/integration-audits")
public class IntegrationAuditController {
    private final AuthService authService;
    private final IntegrationAuditService auditService;

    public IntegrationAuditController(AuthService authService, IntegrationAuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) Boolean success,
            @RequestParam(defaultValue = "100") int limit,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return Map.of("items", auditService.list(
                authService.requireActor(authorization), protocol, success, limit));
    }
}
