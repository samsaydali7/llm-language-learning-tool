package com.languagelearning.extraction;

import com.languagelearning.extraction.model.ExtractedNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills in each {@link ExtractedNode}'s end page. A node's range ends the page before the next
 * node in document (preorder) order that is not one of its own descendants - i.e. the next
 * sibling, or the parent's next sibling, and so on up the tree. The very last node in the
 * document runs to the book's final page.
 */
final class StructureEndPageResolver {

    private StructureEndPageResolver() {
    }

    static void assignEndPages(List<ExtractedNode> roots, int totalPages) {
        List<Entry> flat = new ArrayList<>();
        for (ExtractedNode root : roots) {
            flatten(root, 0, flat);
        }
        for (int i = 0; i < flat.size(); i++) {
            Entry entry = flat.get(i);
            Integer nextStart = null;
            for (int j = i + 1; j < flat.size(); j++) {
                Entry candidate = flat.get(j);
                if (candidate.depth <= entry.depth && candidate.node.getStartPage() != null) {
                    nextStart = candidate.node.getStartPage();
                    break;
                }
            }
            Integer start = entry.node.getStartPage();
            int end = nextStart != null ? Math.max(nextStart - 1, start == null ? nextStart - 1 : start) : totalPages;
            entry.node.setEndPage(start == null ? null : end);
        }
    }

    private static void flatten(ExtractedNode node, int depth, List<Entry> out) {
        out.add(new Entry(node, depth));
        for (ExtractedNode child : node.getChildren()) {
            flatten(child, depth + 1, out);
        }
    }

    private record Entry(ExtractedNode node, int depth) {
    }
}
