package com.languagelearning.structure;

/**
 * One unit of knowledge-extraction work: either a leaf section's full page range, or a "gap"
 * sub-range of a non-leaf node's own range that no leaf claims (see
 * {@link com.languagelearning.structure.service.StructureNodeService#planKnowledgeExtractionRanges}).
 */
public record KnowledgeCoverageRange(Long structureNodeId, Integer startPage, Integer endPage) {
}
