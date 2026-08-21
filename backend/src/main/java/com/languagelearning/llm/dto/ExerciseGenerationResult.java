package com.languagelearning.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseGenerationResult(List<GeneratedExercise> exercises) {

    public ExerciseGenerationResult {
        exercises = exercises == null ? List.of() : exercises;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneratedExercise(
            String type,
            String prompt,
            List<String> options,
            String correctAnswer,
            String explanation,
            List<Long> sourceKnowledgeItemIds) {

        public GeneratedExercise {
            options = options == null ? List.of() : options;
            sourceKnowledgeItemIds = sourceKnowledgeItemIds == null ? List.of() : sourceKnowledgeItemIds;
        }
    }
}
