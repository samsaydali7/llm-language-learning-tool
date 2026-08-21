package com.languagelearning.attempt.repository;

import com.languagelearning.attempt.entity.KnowledgeReviewState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeReviewStateRepository extends JpaRepository<KnowledgeReviewState, Long> {

    Optional<KnowledgeReviewState> findByKnowledgeItemId(Long knowledgeItemId);

    List<KnowledgeReviewState> findByKnowledgeItem_Book_IdOrderByTimesFailedDescLastFailedAtDesc(Long bookId);

    List<KnowledgeReviewState> findByKnowledgeItem_Book_Language_IdOrderByTimesFailedDescLastFailedAtDesc(Long languageId);
}
