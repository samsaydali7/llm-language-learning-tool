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
        // Connecting to Ollama should fail fast if it's genuinely unreachable, but how long a
        // single generation takes is unbounded on constrained local hardware (measured: a single
        // qwen3:8b call anywhere from ~130s to 300s+ depending on chunk size and what else is
        // competing for the machine) - so the read timeout is deliberately indefinite (0 means
        // "no timeout" per HttpURLConnection) rather than a guess that ends up killing valid,
        // still-working generations.
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ZERO);
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /** Sends a single-shot prompt and returns the raw text response (expected to contain JSON). */
    public String generate(String model, String prompt) {
        // Qwen3 models "think" by default - generating a long hidden reasoning chain before the
        // actual answer - which is expensive on constrained local hardware and not needed for a
        // structured-extraction task like this one. "think" is a no-op for models that don't
        // support it (e.g. Llama), so it's safe to always send.
        GenerateRequest request = new GenerateRequest(model, prompt, false, "json", false, new Options(0.2, 4096));
        long startedAt = System.currentTimeMillis();
        log.info("Ollama call starting: model={} promptChars={}", model, prompt.length());
        GenerateResponse response;
        try {
            response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(GenerateResponse.class);
        } catch (RuntimeException e) {
            log.warn("Ollama call failed after {}s: model={} error={}",
                    elapsedSeconds(startedAt), model, e.toString());
            throw e;
        }
        if (response == null || response.response() == null) {
            log.warn("Ollama call returned no body after {}s: model={}", elapsedSeconds(startedAt), model);
            throw new LlmResponseParseException("Ollama returned no response body for model " + model);
        }
        double elapsed = elapsedSeconds(startedAt);
        Long evalCount = response.evalCount();
        String rate = evalCount != null && elapsed > 0 ? String.format(" (%.1f tok/s)", evalCount / elapsed) : "";
        log.info("Ollama call finished in {}s: model={} responseChars={} evalCount={}{}",
                elapsed, model, response.response().length(), evalCount, rate);
        return response.response();
    }

    private double elapsedSeconds(long startedAtMillis) {
        return Math.round((System.currentTimeMillis() - startedAtMillis) / 100.0) / 10.0;
    }

    public <T> T generateStructured(String model, String prompt, Class<T> type) {
        String raw = generate(model, prompt);
        return JsonExtractor.parse(objectMapper, raw, type);
    }

    private record Options(double temperature, @JsonProperty("num_predict") int numPredict) {
    }

    private record GenerateRequest(String model, String prompt, boolean stream, String format, boolean think, Options options) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GenerateResponse(String response, Boolean done, @JsonProperty("eval_count") Long evalCount) {
    }
}
