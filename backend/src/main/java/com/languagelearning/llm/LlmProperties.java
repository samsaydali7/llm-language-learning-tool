package com.languagelearning.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code llm.*} routing configuration exactly as described in SPEC.md #8: which
 * provider and model handle extraction, exercise generation, and grammar review. Changing which
 * provider/model backs a workload is a deployment concern (env vars / this config), never
 * application code.
 */
@ConfigurationProperties(prefix = "llm")
public record LlmProperties(Workload extraction, Workload exercises, Workload grammarReview) {

    public record Workload(String provider, String model) {
    }
}
