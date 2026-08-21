package com.languagelearning.grammar;

import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.llm.dto.GrammarReviewResult;
import com.languagelearning.scope.api.StudyScopeRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grammar")
@RequiredArgsConstructor
public class GrammarController {

    private final GrammarReviewService grammarReviewService;

    @PostMapping("/prioritized")
    public List<GrammarPointResponse> prioritized(@RequestBody StudyScopeRequest scope) {
        return grammarReviewService.prioritized(scope.toScope()).stream().map(GrammarPointResponse::from).toList();
    }

    @GetMapping("/{grammarPointId}/review")
    public GrammarReviewResult review(@PathVariable Long grammarPointId) {
        return grammarReviewService.review(grammarPointId);
    }

    public record GrammarPointResponse(Long id, String title, String summary, Long bookId, Integer page) {
        static GrammarPointResponse from(KnowledgeItem item) {
            return new GrammarPointResponse(item.getId(), item.getHeadword(), item.getSummary(),
                    item.getBook().getId(), item.getPage());
        }
    }
}
