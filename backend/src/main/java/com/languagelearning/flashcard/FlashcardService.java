package com.languagelearning.flashcard;

import com.languagelearning.knowledge.entity.KnowledgeExample;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.scope.KnowledgeQueryService;
import com.languagelearning.scope.StudyScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds flashcards straight from the knowledge base (REQUIREMENTS.md - "Use flashcards").
 * Deliberately LLM-free so a deck is always instant and available even without a model loaded.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlashcardService {

    private final KnowledgeQueryService knowledgeQueryService;
    private final KnowledgeExampleRepository knowledgeExampleRepository;

    public List<Flashcard> generate(StudyScope scope) {
        List<KnowledgeItem> items = knowledgeQueryService.resolve(scope);
        Map<Long, KnowledgeExample> exampleByItem = new HashMap<>();
        for (KnowledgeExample example : knowledgeExampleRepository.findByKnowledgeItemIdIn(items.stream().map(KnowledgeItem::getId).toList())) {
            exampleByItem.putIfAbsent(example.getKnowledgeItem().getId(), example);
        }
        return items.stream().map(item -> toCard(item, exampleByItem.get(item.getId()))).toList();
    }

    private Flashcard toCard(KnowledgeItem item, KnowledgeExample example) {
        return new Flashcard(
                item.getId(),
                item.getType().name(),
                item.getHeadword(),
                item.getSummary(),
                example != null ? example.getExampleText() : null,
                example != null ? example.getTranslation() : null,
                item.getBook().getId(),
                item.getPage());
    }
}
