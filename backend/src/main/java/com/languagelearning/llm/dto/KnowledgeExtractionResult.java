package com.languagelearning.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Structured knowledge extracted from a section of text. This shape doubles as the JSON schema
 * the extraction model is asked to produce (see extraction/KnowledgePrompts) and is deserialized
 * directly from the model's response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KnowledgeExtractionResult(
        List<VocabularyExtract> vocabulary,
        List<GrammarExtract> grammar,
        List<ExpressionExtract> expressions,
        List<String> topics) {

    public KnowledgeExtractionResult {
        vocabulary = vocabulary == null ? List.of() : vocabulary;
        grammar = grammar == null ? List.of() : grammar;
        expressions = expressions == null ? List.of() : expressions;
        topics = topics == null ? List.of() : topics;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VocabularyExtract(
            String headword,
            String partOfSpeech,
            String meaning,
            String notes,
            List<ExampleExtract> examples) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GrammarExtract(
            String title,
            String explanation,
            String patterns,
            List<ExampleExtract> examples) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpressionExtract(
            String phrase,
            String meaning,
            String usageNotes,
            List<ExampleExtract> examples) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExampleExtract(String text, String translation) {
    }
}
