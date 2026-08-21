package com.languagelearning.knowledge.api;

import com.languagelearning.knowledge.api.KnowledgeDtos.ExampleResponse;
import com.languagelearning.knowledge.api.KnowledgeDtos.KnowledgeItemResponse;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.scope.KnowledgeQueryService;
import com.languagelearning.scope.api.StudyScopeRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge-items")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeQueryService knowledgeQueryService;
    private final KnowledgeExampleRepository knowledgeExampleRepository;

    @PostMapping("/search")
    public List<KnowledgeItemResponse> search(@RequestBody StudyScopeRequest scope) {
        List<KnowledgeItem> items = knowledgeQueryService.resolve(scope.toScope());
        Map<Long, List<ExampleResponse>> examplesByItem = new HashMap<>();
        knowledgeExampleRepository.findByKnowledgeItemIdIn(items.stream().map(KnowledgeItem::getId).toList())
                .forEach(example -> examplesByItem
                        .computeIfAbsent(example.getKnowledgeItem().getId(), k -> new java.util.ArrayList<>())
                        .add(ExampleResponse.from(example)));
        return items.stream()
                .map(item -> KnowledgeItemResponse.from(item, examplesByItem.getOrDefault(item.getId(), List.of())))
                .toList();
    }
}
