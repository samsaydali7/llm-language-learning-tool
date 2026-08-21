package com.languagelearning.job.messaging;

/**
 * @param startPage override for a sub-range of {@code structureNodeId}'s own page span, used to
 *                  cover a parent node's "gap" pages - the ones in its own range but not claimed
 *                  by any of its children (e.g. a chapter's intro text before its first section).
 *                  Null for an ordinary leaf-section message, which uses the node's full range.
 * @param endPage   see {@code startPage}.
 */
public record KnowledgeExtractionMessage(
        Long jobId, Long bookId, Long structureNodeId, Integer startPage, Integer endPage) {

    public KnowledgeExtractionMessage(Long jobId, Long bookId, Long structureNodeId) {
        this(jobId, bookId, structureNodeId, null, null);
    }
}
