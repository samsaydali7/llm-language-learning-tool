package com.languagelearning.extraction;

import com.languagelearning.extraction.model.ExtractedNode;
import com.languagelearning.structure.entity.StructureNodeType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.springframework.stereotype.Component;

/**
 * Detects a book's chapter/section structure from its PDF outline (bookmarks), which publishers
 * typically embed and which is a deterministic, free signal - no LLM call needed to find
 * structure (only to extract knowledge from within it, see extraction/KnowledgePrompts). Outline
 * depth is mapped onto {@link StructureNodeType}: depth 0 = VOLUME, 1 = CHAPTER, 2 = SECTION,
 * 3+ = SUBSECTION, matching the depth of nesting shown in SPEC.md's worked example.
 */
@Component
@Slf4j
public class PdfStructureExtractor {

    private static final StructureNodeType[] TYPES_BY_DEPTH = {
            StructureNodeType.VOLUME, StructureNodeType.CHAPTER, StructureNodeType.SECTION, StructureNodeType.SUBSECTION
    };

    /** Returns an empty list when the PDF has no outline; callers should fall back to {@link HeuristicStructureExtractor}. */
    public List<ExtractedNode> extract(PDDocument document) {
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
        if (outline == null) {
            return List.of();
        }
        List<ExtractedNode> roots = buildChildren(outline, document, 0);
        StructureEndPageResolver.assignEndPages(roots, document.getNumberOfPages());
        return roots;
    }

    private List<ExtractedNode> buildChildren(PDOutlineNode parent, PDDocument document, int depth) {
        List<ExtractedNode> nodes = new ArrayList<>();
        PDOutlineItem item = parent.getFirstChild();
        while (item != null) {
            ExtractedNode node = buildNode(item, document, depth);
            if (node != null) {
                nodes.add(node);
            }
            item = item.getNextSibling();
        }
        return nodes;
    }

    private ExtractedNode buildNode(PDOutlineItem item, PDDocument document, int depth) {
        String title = item.getTitle() == null || item.getTitle().isBlank() ? "Untitled" : item.getTitle().strip();
        Integer startPage = resolvePage(item, document);
        List<ExtractedNode> children = buildChildren(item, document, depth + 1);
        StructureNodeType type = TYPES_BY_DEPTH[Math.min(depth, TYPES_BY_DEPTH.length - 1)];
        return new ExtractedNode(title, type, startPage, children);
    }

    private Integer resolvePage(PDOutlineItem item, PDDocument document) {
        try {
            PDDestination destination = resolveDestination(item, document);
            if (destination instanceof PDPageDestination pageDestination) {
                int pageNumber = pageDestination.retrievePageNumber();
                return pageNumber >= 0 ? pageNumber + 1 : null;
            }
        } catch (IOException e) {
            log.warn("Could not resolve page for outline item '{}': {}", item.getTitle(), e.getMessage());
        }
        return null;
    }

    /**
     * An outline item's target is a {@code /Dest} entry in the simple case, but it is just as
     * common - especially for PDFs produced by ebook-conversion pipelines - for it to carry a
     * {@code /A} (action) entry instead, typically a GoTo action wrapping the real destination.
     * That destination can itself be a named destination requiring a lookup against the
     * document's name tree rather than a direct page reference. Without handling both of these,
     * every outline item silently resolves to "no page", which is what a first version of this
     * shipped with - see PdfStructureExtractorTest for the regression coverage.
     */
    private PDDestination resolveDestination(PDOutlineItem item, PDDocument document) throws IOException {
        PDDestination destination = item.getDestination();
        if (destination == null) {
            PDAction action = item.getAction();
            if (action instanceof PDActionGoTo goTo) {
                destination = goTo.getDestination();
            }
        }
        if (destination instanceof PDNamedDestination named) {
            destination = document.getDocumentCatalog().findNamedDestinationPage(named);
        }
        return destination;
    }
}
