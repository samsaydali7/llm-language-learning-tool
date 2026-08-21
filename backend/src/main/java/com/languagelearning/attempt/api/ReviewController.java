package com.languagelearning.attempt.api;

import com.languagelearning.attempt.entity.KnowledgeReviewState;
import com.languagelearning.attempt.repository.KnowledgeReviewStateRepository;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Backs the "review/failures" dashboard (REQUIREMENTS.md - "Review mistakes"). */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final KnowledgeReviewStateRepository reviewStateRepository;

    @GetMapping("/failures")
    public List<FailureResponse> failures(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long languageId) {
        List<KnowledgeReviewState> states;
        if (bookId != null) {
            states = reviewStateRepository.findByKnowledgeItem_Book_IdOrderByTimesFailedDescLastFailedAtDesc(bookId);
        } else if (languageId != null) {
            states = reviewStateRepository.findByKnowledgeItem_Book_Language_IdOrderByTimesFailedDescLastFailedAtDesc(languageId);
        } else {
            states = List.of();
        }
        return states.stream()
                .filter(s -> s.getTimesFailed() != null && s.getTimesFailed() > 0)
                .map(FailureResponse::from)
                .toList();
    }

    public record FailureResponse(
            Long knowledgeItemId, String type, String headword, String summary,
            Integer timesFailed, String lastFailedAt) {

        static FailureResponse from(KnowledgeReviewState state) {
            KnowledgeItem item = state.getKnowledgeItem();
            return new FailureResponse(
                    item.getId(), item.getType().name(), item.getHeadword(), item.getSummary(),
                    state.getTimesFailed(), state.getLastFailedAt() != null ? state.getLastFailedAt().toString() : null);
        }
    }
}
