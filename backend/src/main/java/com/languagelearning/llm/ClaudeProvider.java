package com.languagelearning.llm;

import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.GrammarReviewRequest;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import org.springframework.stereotype.Component;

/**
 * Seam for a future hosted Anthropic provider (SPEC.md #7-8). Claude cannot run through Ollama -
 * it would require a separate Anthropic API integration, internet access, and an API key. Adding
 * a Claude API dependency is an explicit V1 non-goal, so this bean exists only so that
 * {@code llm.extraction.provider: claude} resolves to a real (if inert) implementation rather than
 * a missing bean; it deliberately does not perform any network call in V1.
 */
@Component
public class ClaudeProvider implements LlmProvider {

    @Override
    public String name() {
        return "claude";
    }

    @Override
    public KnowledgeExtractionResult extractKnowledge(KnowledgeExtractionRequest request) {
        throw unsupported();
    }

    @Override
    public ExerciseGenerationResult generateExercises(ExerciseGenerationRequest request) {
        throw unsupported();
    }

    @Override
    public GrammarReviewResult generateGrammarReview(GrammarReviewRequest request) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(
                "ClaudeProvider is not configured in V1. It is a seam for a future Anthropic API "
                        + "integration; the default local workflow (Qwen/Llama via Ollama) does not require it.");
    }
}
