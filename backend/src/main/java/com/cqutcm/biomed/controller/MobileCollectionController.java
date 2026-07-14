package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.MobileCollectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileCollectionController {
    private final AuthService auth;
    private final MobileCollectionService service;

    public MobileCollectionController(AuthService auth, MobileCollectionService service) {
        this.auth = auth;
        this.service = service;
    }

    @GetMapping("/devices")
    public Map<String, Object> devices(@RequestHeader(value = "Authorization", required = false) String token) {
        return Map.of("items", service.devices(auth.requireActor(token)));
    }

    @PostMapping("/devices")
    public Map<String, Object> register(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        return service.register(body, auth.requireActor(token));
    }

    @PostMapping("/devices/{id}/rotate-token")
    public Map<String, Object> rotate(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        return service.rotate(id, auth.requireActor(token));
    }

    @PutMapping("/devices/{id}/status")
    public Map<String, Object> status(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        service.setStatus(id, String.valueOf(body.get("status")), auth.requireActor(token));
        return Map.of("message", "status updated");
    }

    @GetMapping("/batches")
    public Map<String, Object> batches(@RequestHeader(value = "X-Device-Token", required = false) String token) {
        return Map.of("items", service.activeBatches(token));
    }

    @PostMapping("/growth-records/batch")
    public Map<String, Object> ingest(
            @RequestHeader(value = "X-Device-Token", required = false) String token,
            @RequestBody Map<String, List<Map<String, Object>>> body
    ) {
        return service.ingest(token, body.get("records"));
    }

    @GetMapping("/sync/status")
    public Map<String, Object> sync(@RequestHeader(value = "X-Device-Token", required = false) String token) {
        return service.syncStatus(token);
    }
}
