package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.IntegrationClientService;
import com.cqutcm.biomed.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/integration-clients")
public class IntegrationClientController {
    private final AuthService authService;
    private final IntegrationClientService service;

    public IntegrationClientController(AuthService authService, IntegrationClientService service) {
        this.authService = authService;
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        return Map.of("items", service.list(actor));
    }

    @PostMapping
    public Map<String, Object> register(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return service.register(body, authService.requireActor(authorization));
    }

    @PostMapping("/{id}/rotate-secret")
    public Map<String, Object> rotateSecret(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return service.rotateSecret(id, authService.requireActor(authorization));
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> setStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        service.setStatus(id, String.valueOf(body.get("status")), authService.requireActor(authorization));
        return Map.of("message", "status updated");
    }
}
