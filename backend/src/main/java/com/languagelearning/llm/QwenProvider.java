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
 * Runs Qwen models through the local Ollama runtime. Recommended for extraction workloads, which
 * benefit from a stronger model's instruction-following (SPEC.md #4). A separate bean from
 * {@link LlamaProvider} so routing config can select "qwen" vs "llama" by name (SPEC.md #7), even
 * though both currently delegate to the same {@link OllamaClient}.
 */
@Component
@RequiredArgsConstructor
public class QwenProvider implements LlmProvider {

    private final OllamaClient ollamaClient;

    @Override
    public String name() {
        return "qwen";
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
