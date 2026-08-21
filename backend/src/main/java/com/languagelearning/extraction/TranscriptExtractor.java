package com.languagelearning.extraction;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Extracts the raw text for a page range. The book already contains the written transcripts of
 * its audio (REQUIREMENTS.md - Audio), so this text doubles as both the LLM extraction input and
 * the source scanned for audio markers - no speech-to-text is ever required.
 */
@Component
public class TranscriptExtractor {

    public String extractPages(PDDocument document, int startPage, int endPage) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(Math.max(startPage, 1));
        stripper.setEndPage(Math.min(endPage, document.getNumberOfPages()));
        return stripper.getText(document);
    }

    public String extractPage(PDDocument document, int page) throws IOException {
        return extractPages(document, page, page);
    }
}
