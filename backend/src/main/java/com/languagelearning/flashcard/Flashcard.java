package com.languagelearning.flashcard;

/** A deterministic front/back flashcard built directly from a knowledge item - no LLM call needed. */
public record Flashcard(
        Long knowledgeItemId,
        String type,
        String front,
        String back,
        String exampleText,
        String exampleTranslation,
        Long bookId,
        Integer page) {
}
