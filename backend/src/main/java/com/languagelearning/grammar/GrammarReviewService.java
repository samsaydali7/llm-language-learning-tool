package com.languagelearning.grammar;

import com.languagelearning.attempt.entity.KnowledgeReviewState;
import com.languagelearning.attempt.repository.KnowledgeReviewStateRepository;
import com.languagelearning.common.LanguageNames;
import com.languagelearning.common.exception.InvalidRequestException;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.entity.KnowledgeItemType;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.knowledge.repository.KnowledgeItemRepository;
import com.languagelearning.llm.LlmRouter;
import com.languagelearning.llm.dto.GrammarReviewRequest;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.scope.KnowledgeQueryService;
import com.languagelearning.scope.StudyScope;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reviews a grammar point with the LLM, prioritized so points the learner keeps failing surface
 * first (REQUIREMENTS.md - "Review grammar with examples"; SPEC.md non-goals exclude advanced
 * spaced repetition, so prioritization here is a simple failure-count sort, not a scheduler).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrammarReviewService {

    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeExampleRepository knowledgeExampleRepository;
    private final KnowledgeReviewStateRepository reviewStateRepository;
    private final KnowledgeQueryService knowledgeQueryService;
    private final LlmRouter llmRouter;

    /** Grammar points for a scope, most-failed first. */
    public List<KnowledgeItem> prioritized(StudyScope baseScope) {
        StudyScope grammarScope = new StudyScope(
                baseScope.languageId(), baseScope.bookId(), baseScope.structureNodeIds(), baseScope.topicIds(),
                List.of(KnowledgeItemType.GRAMMAR));
        List<KnowledgeItem> items = knowledgeQueryService.resolve(grammarScope);
        return items.stream()
                .sorted(Comparator.comparingInt(this::failureCount).reversed()
                        .thenComparing(KnowledgeItem::getHeadword))
                .toList();
    }

    public GrammarReviewResult review(Long grammarPointId) {
        KnowledgeItem item = knowledgeItemRepository.findById(grammarPointId)
                .orElseThrow(() -> ResourceNotFoundException.of("GrammarPoint", grammarPointId));
        if (item.getType() != KnowledgeItemType.GRAMMAR) {
            throw new InvalidRequestException("Knowledge item " + grammarPointId + " is not a grammar point");
        }

        List<String> examples = knowledgeExampleRepository.findByKnowledgeItemId(item.getId()).stream()
                .map(e -> e.getExampleText() + (e.getTranslation() != null ? " (" + e.getTranslation() + ")" : ""))
                .toList();

        List<String> failureNotes = reviewStateRepository.findByKnowledgeItemId(item.getId())
                .filter(state -> state.getTimesFailed() != null && state.getTimesFailed() > 0)
                .map(state -> List.of("The learner has answered exercises about this incorrectly "
                        + state.getTimesFailed() + " time(s)."))
                .orElse(List.of());

        GrammarReviewRequest request = new GrammarReviewRequest(
                item.getBook().getLanguage().getName(),
                LanguageNames.displayName(item.getBook().getExplanationLanguageCode()),
                item.getHeadword(),
                item.getSummary(),
                examples,
                failureNotes,
                null);
        return llmRouter.generateGrammarReview(request);
    }

    private int failureCount(KnowledgeItem item) {
        return reviewStateRepository.findByKnowledgeItemId(item.getId())
                .map(KnowledgeReviewState::getTimesFailed)
                .orElse(0);
    }
}
