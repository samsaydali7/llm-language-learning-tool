package com.languagelearning.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.llm.LlmProperties.Workload;
import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlmRouterTest {

    @Test
    void routesEachWorkloadToItsConfiguredProviderAndModel() {
        LlmProvider qwen = mock(LlmProvider.class);
        when(qwen.name()).thenReturn("qwen");
        LlmProvider llama = mock(LlmProvider.class);
        when(llama.name()).thenReturn("llama");

        LlmProperties properties = new LlmProperties(
                new Workload("qwen", "qwen3:8b"),
                new Workload("llama", "llama3.2:3b"),
                new Workload("llama", "llama3.2:3b"));
        LlmRouter router = new LlmRouter(List.of(qwen, llama), properties);

        KnowledgeExtractionRequest extractionRequest =
                new KnowledgeExtractionRequest("French", "English", "Book", "Section", "text", 1, null);
        when(qwen.extractKnowledge(any())).thenReturn(new KnowledgeExtractionResult(null, null, null, null));

        router.extractKnowledge(extractionRequest);

        var captor = org.mockito.ArgumentCaptor.forClass(KnowledgeExtractionRequest.class);
        verify(qwen).extractKnowledge(captor.capture());
        assertThat(captor.getValue().model()).isEqualTo("qwen3:8b");

        ExerciseGenerationRequest exerciseRequest =
                new ExerciseGenerationRequest("French", "English", List.of("MULTIPLE_CHOICE"), 3, List.of(), null, null);
        when(llama.generateExercises(any())).thenReturn(new ExerciseGenerationResult(null));
        router.generateExercises(exerciseRequest);

        var exerciseCaptor = org.mockito.ArgumentCaptor.forClass(ExerciseGenerationRequest.class);
        verify(llama).generateExercises(exerciseCaptor.capture());
        assertThat(exerciseCaptor.getValue().model()).isEqualTo("llama3.2:3b");
    }

    @Test
    void throwsWhenNoProviderIsRegisteredForTheConfiguredName() {
        LlmProperties properties = new LlmProperties(
                new Workload("does-not-exist", "some-model"), null, null);
        LlmRouter router = new LlmRouter(List.of(), properties);

        assertThatThrownBy(() -> router.extractKnowledge(
                new KnowledgeExtractionRequest("French", "English", "Book", "Section", "text", 1, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does-not-exist");
    }
}
