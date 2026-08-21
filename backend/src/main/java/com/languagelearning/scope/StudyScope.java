package com.languagelearning.scope;

import com.languagelearning.knowledge.entity.KnowledgeItemType;
import java.util.List;

/**
 * A combinable selection of "what to study" (REQUIREMENTS.md - "Choosing What to Study"): any
 * mix of language, book, structure nodes (chapter/section/...), topics, and knowledge types. Null
 * or empty on a field means "no filter on that dimension". This single shape backs browsing,
 * both exercise-generation modes, flashcards, and listening selection.
 */
public record StudyScope(
        Long languageId,
        Long bookId,
        List<Long> structureNodeIds,
        List<Long> topicIds,
        List<KnowledgeItemType> knowledgeTypes,
        List<Long> knowledgeItemIds) {

    public StudyScope {
        structureNodeIds = structureNodeIds == null ? List.of() : structureNodeIds;
        topicIds = topicIds == null ? List.of() : topicIds;
        knowledgeTypes = knowledgeTypes == null ? List.of() : knowledgeTypes;
        knowledgeItemIds = knowledgeItemIds == null ? List.of() : knowledgeItemIds;
    }

    /** Convenience constructor for the common case of not filtering by explicit item ids. */
    public StudyScope(Long languageId, Long bookId, List<Long> structureNodeIds, List<Long> topicIds,
            List<KnowledgeItemType> knowledgeTypes) {
        this(languageId, bookId, structureNodeIds, topicIds, knowledgeTypes, List.of());
    }
}
