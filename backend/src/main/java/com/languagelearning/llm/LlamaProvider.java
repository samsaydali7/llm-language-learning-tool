package com.languagelearning.llm;

import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.GrammarReviewRequest;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import com.languagelearning.llm.prompt.ExercisePrompts;
import com.languagelearning.llm.prompt.GrammarReviewPrompts;
import com.languagelearning.llm.prompt.KnowledgePrompts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Runs Llama models through the local Ollama runtime. Recommended for exercise generation and
 * grammar review, which run frequently and benefit from a fast, low-memory local model
 * (SPEC.md #4-5). A separate bean from {@link QwenProvider} for routing purposes.
 */
@Component
@RequiredArgsConstructor
public class LlamaProvider implements LlmProvider {

    private final OllamaClient ollamaClient;

    @Override
    public String name() {
        return "llama";
    }

    @Override
    public KnowledgeExtractionResult extractKnowledge(KnowledgeExtractionRequest request) {
        String prompt = KnowledgePrompts.build(request);
        return ollamaClient.generateStructured(request.model(), prompt, KnowledgeExtractionResult.class);
    }

    @Override
    public ExerciseGenerationResult generateExercises(ExerciseGenerationRequest request) {
        String prompt = ExercisePrompts.build(request);
        return ollamaClient.generateStructured(request.model(), prompt, ExerciseGenerationResult.class);
    }

    @Override
    public GrammarReviewResult generateGrammarReview(GrammarReviewRequest request) {
        String prompt = GrammarReviewPrompts.build(request);
        return ollamaClient.generateStructured(request.model(), prompt, GrammarReviewResult.class);
    }
}
