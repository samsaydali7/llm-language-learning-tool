package com.languagelearning.structure.api;

import com.languagelearning.structure.api.StructureDtos.StructureNodeResponse;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.structure.service.StructureNodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StructureController {

    private final StructureNodeService structureNodeService;

    @GetMapping("/api/books/{bookId}/structure")
    public List<StructureNodeResponse> roots(@PathVariable Long bookId) {
        return toResponses(structureNodeService.getRoots(bookId));
    }

    @GetMapping("/api/structure-nodes/{id}")
    public StructureNodeResponse get(@PathVariable Long id) {
        StructureNode node = structureNodeService.getById(id);
        boolean hasChildren = !structureNodeService.getChildren(id).isEmpty();
        return StructureNodeResponse.from(node, hasChildren);
    }

    @GetMapping("/api/structure-nodes/{id}/children")
    public List<StructureNodeResponse> children(@PathVariable Long id) {
        return toResponses(structureNodeService.getChildren(id));
    }

    private List<StructureNodeResponse> toResponses(List<StructureNode> nodes) {
        return nodes.stream()
                .map(node -> StructureNodeResponse.from(node, !structureNodeService.getChildren(node.getId()).isEmpty()))
                .toList();
    }
}
