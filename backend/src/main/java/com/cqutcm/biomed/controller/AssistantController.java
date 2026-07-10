package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService service;
    private final com.cqutcm.biomed.service.AuthService authService;

    public AssistantController(AssistantService service, com.cqutcm.biomed.service.AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.requireActor(authorization);
        return service.chat(request.get("question"));
    }
}
