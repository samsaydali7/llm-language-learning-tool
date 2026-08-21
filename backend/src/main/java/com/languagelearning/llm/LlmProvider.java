package com.languagelearning.llm;

import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.GrammarReviewRequest;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;

/**
 * Abstraction over the model that performs a given workload (SPEC.md #7). Application code must
 * depend only on this interface (or {@link LlmRouter}), never on a concrete provider, so the
 * extraction model and exercise model can be routed to different providers independently. The
 * concrete model name to use travels on each request's {@code model} field, populated by
 * {@link LlmRouter} from the {@code llm.*} routing configuration (SPEC.md #8).
 */
public interface LlmProvider {

    /** Identifies this provider for routing/config purposes, e.g. "qwen", "llama", "claude". */
    String name();

    KnowledgeExtractionResult extractKnowledge(KnowledgeExtractionRequest request);

    ExerciseGenerationResult generateExercises(ExerciseGenerationRequest request);

    GrammarReviewResult generateGrammarReview(GrammarReviewRequest request);
}
