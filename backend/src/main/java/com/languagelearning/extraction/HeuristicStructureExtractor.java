package com.languagelearning.extraction;

import com.languagelearning.extraction.model.ExtractedNode;
import com.languagelearning.structure.entity.StructureNodeType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

/**
 * Fallback structure detection for PDFs with no outline/bookmarks. Flags short lines whose font
 * size is notably larger than the page's typical body text as chapter headings. This is
 * necessarily a heuristic (font size is the only reliably available signal without an LLM call),
 * so it only produces a single, flat level of CHAPTER nodes - it does not attempt to infer
 * section/subsection nesting the way outline-based extraction can.
 */
@Component
@Slf4j
public class HeuristicStructureExtractor {

    private static final double HEADING_SIZE_RATIO = 1.15;
    private static final int MAX_HEADING_LENGTH = 90;
    private static final int MIN_HEADING_LENGTH = 3;

    public List<ExtractedNode> extract(PDDocument document) throws IOException {
        HeadingCollectingStripper stripper = new HeadingCollectingStripper();
        stripper.setSortByPosition(true);
        stripper.getText(document);

        double bodyMedian = median(stripper.lineFontSizes);
        List<ExtractedNode> nodes = new ArrayList<>();
        for (HeadingCandidate candidate : stripper.headingCandidates) {
            if (candidate.fontSize >= bodyMedian * HEADING_SIZE_RATIO
                    && candidate.text.length() >= MIN_HEADING_LENGTH
                    && candidate.text.length() <= MAX_HEADING_LENGTH) {
                nodes.add(new ExtractedNode(candidate.text, StructureNodeType.CHAPTER, candidate.page, null));
            }
        }
        log.info("Heuristic structure extraction found {} candidate heading(s) out of {} lines "
                + "(body median font size {})", nodes.size(), stripper.lineFontSizes.size(), bodyMedian);
        StructureEndPageResolver.assignEndPages(nodes, document.getNumberOfPages());
        return nodes;
    }

    private double median(List<Float> values) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Float> sorted = new ArrayList<>(values);
        sorted.sort(Float::compareTo);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0 : sorted.get(mid);
    }

    private record HeadingCandidate(String text, float fontSize, int page) {
    }

    private static final class HeadingCollectingStripper extends PDFTextStripper {

        private final List<HeadingCandidate> headingCandidates = new ArrayList<>();
        private final List<Float> lineFontSizes = new ArrayList<>();
        private int currentPage = 0;

        private HeadingCollectingStripper() throws IOException {
            super();
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            currentPage++;
            super.startPage(page);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (text != null && !text.isBlank() && !textPositions.isEmpty()) {
                float maxSize = 0f;
                for (TextPosition position : textPositions) {
                    maxSize = Math.max(maxSize, position.getFontSizeInPt());
                }
                lineFontSizes.add(maxSize);
                headingCandidates.add(new HeadingCandidate(text.strip(), maxSize, currentPage));
            }
            super.writeString(text, textPositions);
        }
    }
}
