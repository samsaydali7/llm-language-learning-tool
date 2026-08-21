package com.languagelearning.structure.service;

import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.structure.KnowledgeCoverageRange;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.structure.repository.StructureNodeRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

    /**
     * The full set of knowledge-extraction work for a book: one range per leaf section, plus one
     * range per "gap" - a page range no leaf claims (typically a chapter/section's own intro text
     * before its first child begins), anchored to the most specific node that does cover it. This
     * is the single source of truth used both when a book is first ingested and when extraction
     * needs to be resumed/retried against already-detected structure (e.g. after losing queued
     * work to an infrastructure restart, without re-parsing the PDF).
     */
    public List<KnowledgeCoverageRange> planKnowledgeExtractionRanges(Long bookId, int totalPages) {
        List<StructureNode> allNodes = structureNodeRepository.findByBookIdOrderByOrderIndexAsc(bookId);
        List<StructureNode> leaves = getLeaves(bookId);

        List<KnowledgeCoverageRange> ranges = new ArrayList<>();
        for (StructureNode leaf : leaves) {
            ranges.add(new KnowledgeCoverageRange(leaf.getId(), leaf.getStartPage(), leaf.getEndPage()));
        }
        ranges.addAll(computeGapRanges(allNodes, leaves, totalPages));
        return ranges;
    }

    private List<KnowledgeCoverageRange> computeGapRanges(List<StructureNode> allNodes, List<StructureNode> leaves, int totalPages) {
        if (totalPages <= 0) {
            return List.of();
        }
        boolean[] covered = new boolean[totalPages + 1];
        for (StructureNode leaf : leaves) {
            if (leaf.getStartPage() == null) {
                continue;
            }
            int end = leaf.getEndPage() != null ? leaf.getEndPage() : leaf.getStartPage();
            for (int page = leaf.getStartPage(); page <= Math.min(end, totalPages); page++) {
                covered[page] = true;
            }
        }

        List<KnowledgeCoverageRange> ranges = new ArrayList<>();
        int page = 1;
        while (page <= totalPages) {
            if (covered[page]) {
                page++;
                continue;
            }
            int gapStart = page;
            while (page <= totalPages && !covered[page]) {
                page++;
            }
            int gapEnd = page - 1;
            StructureNode anchor = findNodeForPage(allNodes, gapStart);
            if (anchor != null) {
                ranges.add(new KnowledgeCoverageRange(anchor.getId(), gapStart, gapEnd));
            }
        }
        return ranges;
    }

    private StructureNode findNodeForPage(List<StructureNode> nodes, int page) {
        StructureNode best = null;
        for (StructureNode node : nodes) {
            if (node.getStartPage() != null && node.getStartPage() <= page
                    && (node.getEndPage() == null || page <= node.getEndPage())) {
                if (best == null || node.getStartPage() > best.getStartPage()) {
                    best = node;
                }
            }
        }
        return best;
    }
}
