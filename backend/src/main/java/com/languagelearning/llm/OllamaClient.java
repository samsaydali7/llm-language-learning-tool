package com.languagelearning.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP wrapper around a locally-running Ollama server's {@code /api/generate} endpoint.
 * This is the only class in the application that speaks Ollama's wire format; every provider
 * that runs through Ollama (Qwen, Llama) delegates to it. Ollama is used because it is the local
 * runtime for on-device models (SPEC.md #4); it is never used for a hosted provider like Claude.
 */
@Component
@Slf4j
public class OllamaClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaClient(OllamaProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(properties.requestTimeoutSeconds());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /** Sends a single-shot prompt and returns the raw text response (expected to contain JSON). */
    public String generate(String model, String prompt) {
        GenerateRequest request = new GenerateRequest(model, prompt, false, "json", new Options(0.2, 4096));
        log.debug("Calling Ollama model={} promptChars={}", model, prompt.length());
        GenerateResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(GenerateResponse.class);
        if (response == null || response.response() == null) {
            throw new LlmResponseParseException("Ollama returned no response body for model " + model);
        }
        return response.response();
    }

    public <T> T generateStructured(String model, String prompt, Class<T> type) {
        String raw = generate(model, prompt);
        return JsonExtractor.parse(objectMapper, raw, type);
    }

    private record Options(double temperature, @JsonProperty("num_predict") int numPredict) {
    }

    private record GenerateRequest(String model, String prompt, boolean stream, String format, Options options) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GenerateResponse(String response, Boolean done) {
    }
}
