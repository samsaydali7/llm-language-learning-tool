package com.languagelearning.llm.dto;

import java.util.List;

/**
 * @param exerciseTypes allowed exercise type names (e.g. "MULTIPLE_CHOICE", "FILL_IN_BLANK");
 *                       the model picks among these per item
 * @param count          how many exercises to generate
 * @param knowledge      the scoped knowledge items to generate exercises from
 * @param listeningContext optional transcript text when generating listening comprehension items
 */
public record ExerciseGenerationRequest(
        String learningLanguageName,
        String explanationLanguageName,
        List<String> exerciseTypes,
        int count,
        List<KnowledgeSnippet> knowledge,
        String listeningContext,
        String model) {

    public ExerciseGenerationRequest withModel(String newModel) {
        return new ExerciseGenerationRequest(
                learningLanguageName, explanationLanguageName, exerciseTypes, count, knowledge, listeningContext,
                newModel);
    }
}
