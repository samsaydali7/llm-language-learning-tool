package com.languagelearning.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.extraction.model.ExtractedNode;
import com.languagelearning.structure.entity.StructureNodeType;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.junit.jupiter.api.Test;

class PdfStructureExtractorTest {

    private final PdfStructureExtractor extractor = new PdfStructureExtractor();

    @Test
    void buildsATreeFromTheOutlineAndFillsInEndPages() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage[] pages = new PDPage[6];
            for (int i = 0; i < pages.length; i++) {
                pages[i] = new PDPage();
                document.addPage(pages[i]);
            }

            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);

            PDOutlineItem chapter1 = outlineItem("Chapter 1", pages[0]);
            PDOutlineItem chapter2 = outlineItem("Chapter 2", pages[3]);
            outline.addLast(chapter1);
            outline.addLast(chapter2);

            PDOutlineItem section1a = outlineItem("Section 1a", pages[1]);
            chapter1.addLast(section1a);

            List<ExtractedNode> tree = extractor.extract(document);

            assertThat(tree).hasSize(2);
            ExtractedNode node1 = tree.get(0);
            ExtractedNode node2 = tree.get(1);

            assertThat(node1.getTitle()).isEqualTo("Chapter 1");
            assertThat(node1.getType()).isEqualTo(StructureNodeType.VOLUME);
            assertThat(node1.getStartPage()).isEqualTo(1);
            assertThat(node1.getEndPage()).isEqualTo(3);
            assertThat(node1.getChildren()).hasSize(1);
            assertThat(node1.getChildren().get(0).getType()).isEqualTo(StructureNodeType.CHAPTER);
            assertThat(node1.getChildren().get(0).getStartPage()).isEqualTo(2);
            assertThat(node1.getChildren().get(0).getEndPage()).isEqualTo(3);

            assertThat(node2.getTitle()).isEqualTo("Chapter 2");
            assertThat(node2.getStartPage()).isEqualTo(4);
            assertThat(node2.getEndPage()).isEqualTo(6);
        }
    }

    @Test
    void returnsEmptyListWhenThereIsNoOutline() throws Exception {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            assertThat(extractor.extract(document)).isEmpty();
        }
    }

    /**
     * Regression test: real-world PDFs (very commonly ones produced by ebook-conversion
     * pipelines) frequently give an outline item a GoTo *action* rather than a direct
     * destination. The original implementation only checked getDestination(), so every such
     * item silently resolved to "no page" - which meant every section defaulted to page 1 and
     * produced no knowledge (see the "no knowledge extracted" investigation).
     */
    @Test
    void resolvesAPageFromAGoToActionWhenThereIsNoDirectDestination() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            document.addPage(new PDPage());

            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);

            PDOutlineItem item = new PDOutlineItem();
            item.setTitle("Chapter via action");
            PDPageFitWidthDestination destination = new PDPageFitWidthDestination();
            destination.setPage(page);
            PDActionGoTo goTo = new PDActionGoTo();
            goTo.setDestination(destination);
            item.setAction(goTo);
            outline.addLast(item);

            List<ExtractedNode> tree = extractor.extract(document);

            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).getStartPage()).isEqualTo(1);
        }
    }

    private PDOutlineItem outlineItem(String title, PDPage page) {
        PDOutlineItem item = new PDOutlineItem();
        item.setTitle(title);
        PDPageFitWidthDestination destination = new PDPageFitWidthDestination();
        destination.setPage(page);
        item.setDestination(destination);
        return item;
    }
}
