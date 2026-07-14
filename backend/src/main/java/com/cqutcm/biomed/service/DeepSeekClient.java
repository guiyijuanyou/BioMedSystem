package com.cqutcm.biomed.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final int maxTokens;

    public DeepSeekClient(RestClient.Builder builder,
                          @Value("${app.ai.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                          @Value("${app.ai.deepseek.api-key:}") String apiKey,
                          @Value("${app.ai.deepseek.model:deepseek-v4-flash}") String model,
                          @Value("${app.ai.deepseek.max-tokens:1200}") int maxTokens,
                          @Value("${app.ai.deepseek.connect-timeout-ms:10000}") int connectTimeoutMs,
                          @Value("${app.ai.deepseek.read-timeout-ms:60000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public String chat(List<AssistantService.ChatMessage> messages) {
        if (apiKey.isBlank()) {
            throw new AiServiceUnavailableException("AI 助手尚未配置 DEEPSEEK_API_KEY");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("thinking", Map.of("type", "disabled"));
        body.put("max_tokens", maxTokens);
        body.put("stream", false);

        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String answer = response == null ? null : response.path("choices").path(0)
                    .path("message").path("content").asText(null);
            if (answer == null || answer.isBlank()) {
                throw new AiServiceUnavailableException("DeepSeek 返回了空响应，请稍后重试");
            }
            return answer.trim();
        } catch (AiServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AiServiceUnavailableException("DeepSeek 服务调用失败，请稍后重试", ex);
        }
    }

    public String model() {
        return model;
    }
}
