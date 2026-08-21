package com.languagelearning.llm.dto;

import java.util.List;

/**
 * @param grammarTitle title of the grammar point being reviewed
 * @param explanation  the original explanation extracted from the book
 * @param examples     example sentences originally captured for this grammar point
 * @param failureNotes short notes on how the learner has been getting this wrong (may be empty)
 */
public record GrammarReviewRequest(
        String learningLanguageName,
        String explanationLanguageName,
        String grammarTitle,
        String explanation,
        List<String> examples,
        List<String> failureNotes,
        String model) {

    public GrammarReviewRequest withModel(String newModel) {
        return new GrammarReviewRequest(
                learningLanguageName, explanationLanguageName, grammarTitle, explanation, examples, failureNotes,
                newModel);
    }
}
