package com.languagelearning.llm;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OllamaProvider implements LlmProvider {
    private final RestClient client;
    private final String exerciseModel;

    public OllamaProvider(
            RestClient.Builder builder,
            @Value("${ollama.base-url:http://host.docker.internal:11434}") String baseUrl,
            @Value("${llm.exercise-model:llama3.2:3b}") String exerciseModel) {
        this.client = builder.baseUrl(baseUrl).build();
        this.exerciseModel = exerciseModel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> listModels() {
        Map<String, Object> response = client.get().uri("/api/tags").retrieve().body(Map.class);
        if (response == null || !(response.get("models") instanceof List<?> models)) {
            return List.of();
        }
        return models.stream()
                .filter(Map.class::isInstance)
                .map(model -> String.valueOf(((Map<String, Object>) model).get("name")))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String generate(String model, String prompt, int maxTokens) {
        Map<String, Object> request = Map.of(
                "model", model == null || model.isBlank() ? exerciseModel : model,
                "prompt", prompt,
                "stream", false,
                "think", false,
                "options", Map.of("num_predict", maxTokens, "temperature", 0.2));
        Map<String, Object> response = client.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);
        return response == null ? "" : String.valueOf(response.getOrDefault("response", ""));
    }
}
