package com.languagelearning.llm.dto;

/**
 * A flattened, provider-agnostic view of one knowledge item (vocabulary/grammar/expression) used
 * as generation context. {@code referenceId} lets the caller re-link generated exercises back to
 * the originating {@code KnowledgeItem} without the llm package depending on the knowledge domain.
 */
public record KnowledgeSnippet(
        Long referenceId,
        String type,
        String headword,
        String meaning,
        String exampleText) {
}
