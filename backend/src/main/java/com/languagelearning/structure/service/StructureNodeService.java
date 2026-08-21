package com.languagelearning.structure.service;

import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.structure.repository.StructureNodeRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StructureNodeService {

    private final StructureNodeRepository structureNodeRepository;

    public StructureNode getById(Long id) {
        return structureNodeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("StructureNode", id));
    }

    public List<StructureNode> getRoots(Long bookId) {
        return structureNodeRepository.findByBookIdAndParentIsNullOrderByOrderIndexAsc(bookId);
    }

    public List<StructureNode> getChildren(Long parentId) {
        return structureNodeRepository.findByParentIdOrderByOrderIndexAsc(parentId);
    }

    public List<StructureNode> getLeaves(Long bookId) {
        return structureNodeRepository.findLeafNodes(bookId);
    }

    /**
     * Expands a set of selected node ids to include every descendant, so selecting "Chapter 4"
     * also pulls in all of its sections/subsections when resolving a study scope.
     */
    public Set<Long> expandWithDescendants(Long bookId, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return Set.of();
        }
        List<StructureNode> allNodes = structureNodeRepository.findByBookIdOrderByOrderIndexAsc(bookId);
        var childrenByParent = allNodes.stream()
                .filter(n -> n.getParent() != null)
                .collect(Collectors.groupingBy(n -> n.getParent().getId()));

        Set<Long> result = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>(selectedIds);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!result.add(current)) {
                continue;
            }
            for (StructureNode child : childrenByParent.getOrDefault(current, List.of())) {
                queue.add(child.getId());
            }
        }
        return result;
    }
}
