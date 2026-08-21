package com.languagelearning.scope;

import com.languagelearning.knowledge.entity.ExpressionItem;
import com.languagelearning.knowledge.entity.GrammarPoint;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.entity.KnowledgeItemType;
import com.languagelearning.knowledge.entity.VocabularyItem;
import com.languagelearning.knowledge.repository.KnowledgeItemRepository;
import com.languagelearning.structure.service.StructureNodeService;
import com.languagelearning.topic.entity.Topic;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves a {@link StudyScope} into the matching {@link KnowledgeItem}s. This is the single
 * shared query path used by browsing, both exercise-generation modes, flashcards, and listening
 * (SPEC.md #2.3 - "share the same generation pipeline").
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeQueryService {

    private final KnowledgeItemRepository knowledgeItemRepository;
    private final StructureNodeService structureNodeService;

    public List<KnowledgeItem> resolve(StudyScope scope) {
        Specification<KnowledgeItem> spec = toSpecification(scope);
        return knowledgeItemRepository.findAll(spec);
    }

    public Specification<KnowledgeItem> toSpecification(StudyScope scope) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (scope.bookId() != null) {
                predicates.add(cb.equal(root.get("book").get("id"), scope.bookId()));
            } else if (scope.languageId() != null) {
                predicates.add(cb.equal(root.get("book").get("language").get("id"), scope.languageId()));
            }

            if (!scope.structureNodeIds().isEmpty() && scope.bookId() != null) {
                Set<Long> expanded = structureNodeService.expandWithDescendants(scope.bookId(), scope.structureNodeIds());
                predicates.add(root.get("structureNode").get("id").in(expanded));
            }

            if (!scope.topicIds().isEmpty()) {
                var topicJoin = root.join("topics");
                predicates.add(topicJoin.get("id").in(scope.topicIds()));
                query.distinct(true);
            }

            if (!scope.knowledgeItemIds().isEmpty()) {
                predicates.add(root.get("id").in(scope.knowledgeItemIds()));
            }

            if (!scope.knowledgeTypes().isEmpty()) {
                List<Predicate> typePredicates = scope.knowledgeTypes().stream()
                        .map(type -> cb.equal(root.type(), entityClassFor(type)))
                        .toList();
                predicates.add(cb.or(typePredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Every knowledge item belonging to a topic, regardless of book, for topic-based study (REQUIREMENTS.md - Topics). */
    public List<KnowledgeItem> resolveByTopic(Long topicId) {
        return resolve(new StudyScope(null, null, List.of(), List.of(topicId), List.of()));
    }

    private Class<? extends KnowledgeItem> entityClassFor(KnowledgeItemType type) {
        return switch (type) {
            case VOCABULARY -> VocabularyItem.class;
            case GRAMMAR -> GrammarPoint.class;
            case EXPRESSION -> ExpressionItem.class;
        };
    }
}
