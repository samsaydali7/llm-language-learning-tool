package com.languagelearning.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GrammarReviewResult(
        String summary,
        List<String> keyPoints,
        List<String> commonMistakes,
        List<GrammarReviewExample> reinforcementExamples) {

    public GrammarReviewResult {
        keyPoints = keyPoints == null ? List.of() : keyPoints;
        commonMistakes = commonMistakes == null ? List.of() : commonMistakes;
        reinforcementExamples = reinforcementExamples == null ? List.of() : reinforcementExamples;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GrammarReviewExample(String text, String translation) {
    }
}
