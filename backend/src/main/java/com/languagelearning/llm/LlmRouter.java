package com.languagelearning.llm;

import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.GrammarReviewRequest;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * The single entry point application code should use to reach an {@link LlmProvider}. Resolves
 * "which provider, which model" per workload from {@link LlmProperties} (SPEC.md #8) so that
 * extraction, exercise generation, and grammar review can each be routed independently -
 * including to different providers - without any application code change.
 */
@Component
public class LlmRouter {

    private final Map<String, LlmProvider> providersByName;
    private final LlmProperties properties;

    public LlmRouter(List<LlmProvider> providers, LlmProperties properties) {
        this.providersByName = providers.stream()
                .collect(Collectors.toMap(LlmProvider::name, Function.identity()));
        this.properties = properties;
    }

    public KnowledgeExtractionResult extractKnowledge(KnowledgeExtractionRequest request) {
        LlmProperties.Workload workload = requireWorkload(properties.extraction(), "llm.extraction");
        return resolve(workload).extractKnowledge(request.withModel(workload.model()));
    }

    public ExerciseGenerationResult generateExercises(ExerciseGenerationRequest request) {
        LlmProperties.Workload workload = requireWorkload(properties.exercises(), "llm.exercises");
        return resolve(workload).generateExercises(request.withModel(workload.model()));
    }

    public GrammarReviewResult generateGrammarReview(GrammarReviewRequest request) {
        LlmProperties.Workload workload = requireWorkload(properties.grammarReview(), "llm.grammar-review");
        return resolve(workload).generateGrammarReview(request.withModel(workload.model()));
    }

    private LlmProperties.Workload requireWorkload(LlmProperties.Workload workload, String key) {
        if (workload == null || workload.provider() == null || workload.model() == null) {
            throw new IllegalStateException("Missing LLM routing configuration for " + key);
        }
        return workload;
    }

    private LlmProvider resolve(LlmProperties.Workload workload) {
        LlmProvider provider = providersByName.get(workload.provider().toLowerCase());
        if (provider == null) {
            throw new IllegalStateException("No LlmProvider registered for provider name '"
                    + workload.provider() + "'. Known providers: " + providersByName.keySet());
        }
        return provider;
    }
}
