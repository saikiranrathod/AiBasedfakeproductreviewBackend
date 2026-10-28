
package com.fakereview.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;

    public GeminiService(
            @Value("${spring.ai.openai.api-key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader(
                        "Authorization", "Bearer " + apiKey)
                .build();
    }

    public String askGemini(String question) {

        Map<String, Object> request = Map.of(
                "model", "openrouter/free",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", question
                        )
                )
        );

        Map<?, ?> response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "OpenRouter returned an empty response");
        }

        Object choicesObject = response.get("choices");

        if (!(choicesObject instanceof List<?> choices)
                || choices.isEmpty()) {
            throw new IllegalStateException(
                    "OpenRouter returned no answer");
        }

        Object firstObject = choices.get(0);

        if (!(firstObject instanceof Map<?, ?> first)) {
            throw new IllegalStateException(
                    "Invalid OpenRouter response");
        }

        Object messageObject = first.get("message");

        if (!(messageObject instanceof Map<?, ?> message)) {
            throw new IllegalStateException(
                    "OpenRouter response has no message");
        }

        Object content = message.get("content");

        if (!(content instanceof String answer)
                || answer.isBlank()) {
            throw new IllegalStateException(
                    "OpenRouter returned empty content");
        }

        return answer;
    }
}
