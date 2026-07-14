package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AssistantService;
import com.cqutcm.biomed.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService service;
    private final AuthService authService;

    public AssistantController(AssistantService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.requireActor(authorization);
        List<AssistantService.ChatMessage> history = request.history() == null
                ? List.of()
                : request.history().stream()
                .map(item -> new AssistantService.ChatMessage(item.role(), item.content()))
                .toList();
        return service.chat(request.question(), history);
    }

    public record ChatRequest(
            @NotBlank(message = "问题不能为空")
            @Size(max = 2_000, message = "问题不能超过 2000 个字符")
            String question,
            @Size(max = 20, message = "历史消息不能超过 20 条")
            List<HistoryMessage> history
    ) {
    }

    public record HistoryMessage(String role, String content) {
    }
}
