package com.languagelearning.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.ollama")
public record OllamaProperties(String baseUrl, Integer requestTimeoutSeconds) {

    public OllamaProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }
        if (requestTimeoutSeconds == null) {
            requestTimeoutSeconds = 120;
        }
    }
}
