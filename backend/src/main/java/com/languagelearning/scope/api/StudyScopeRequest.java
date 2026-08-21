package com.languagelearning.scope.api;

import com.languagelearning.knowledge.entity.KnowledgeItemType;
import com.languagelearning.scope.StudyScope;
import java.util.List;

/** Wire shape for a {@link StudyScope}, used by every endpoint that accepts a study selection. */
public record StudyScopeRequest(
        Long languageId,
        Long bookId,
        List<Long> structureNodeIds,
        List<Long> topicIds,
        List<KnowledgeItemType> knowledgeTypes,
        List<Long> knowledgeItemIds) {

    public StudyScope toScope() {
        return new StudyScope(languageId, bookId, structureNodeIds, topicIds, knowledgeTypes, knowledgeItemIds);
    }
}
