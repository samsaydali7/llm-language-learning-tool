package com.languagelearning.exercise.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.scope.api.StudyScopeRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ExerciseDtos {

    private ExerciseDtos() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record GenerateExercisesRequest(
            @NotNull StudyScopeRequest scope,
            @Min(1) int count,
            List<ExerciseType> exerciseTypes,
            boolean persist) {
    }

    public record CreateExerciseJobRequest(
            @NotNull StudyScopeRequest scope,
            @Min(1) int count,
            List<ExerciseType> exerciseTypes) {
    }

    public record SubmitAttemptRequest(String answer) {
    }

    public record AttemptResultResponse(boolean correct, String correctAnswer, String explanation) {
    }

    public record ExerciseResponse(
            Long id,
            Long bookId,
            String type,
            String prompt,
            List<String> options,
            String correctAnswer,
            String explanation,
            Long audioFileId,
            List<Long> sourceKnowledgeItemIds) {

        public static ExerciseResponse from(Exercise exercise) {
            List<String> options;
            try {
                options = exercise.getOptionsJson() == null
                        ? List.of() : List.of(MAPPER.readValue(exercise.getOptionsJson(), String[].class));
            } catch (Exception e) {
                options = List.of();
            }
            return new ExerciseResponse(
                    exercise.getId(),
                    exercise.getBook() != null ? exercise.getBook().getId() : null,
                    exercise.getType().name(),
                    exercise.getPrompt(),
                    options,
                    exercise.getCorrectAnswer(),
                    exercise.getExplanation(),
                    exercise.getAudioFile() != null ? exercise.getAudioFile().getId() : null,
                    exercise.getSourceKnowledgeItems().stream().map(item -> item.getId()).toList());
        }
    }
}
