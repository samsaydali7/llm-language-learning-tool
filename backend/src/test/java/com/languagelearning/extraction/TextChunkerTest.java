package com.languagelearning.extraction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    @Test
    void returnsEmptyListForBlankInput() {
        assertThat(TextChunker.chunk("   ", 100)).isEmpty();
        assertThat(TextChunker.chunk(null, 100)).isEmpty();
    }

    @Test
    void keepsShortTextAsOneChunk() {
        String text = "Paragraph one.\n\nParagraph two.";
        List<String> chunks = TextChunker.chunk(text, 1000);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).contains("Paragraph one.").contains("Paragraph two.");
    }

    @Test
    void splitsOnParagraphBoundariesWhenOverLimit() {
        String paragraph = "word ".repeat(20).strip();
        String text = String.join("\n\n", paragraph, paragraph, paragraph);
        List<String> chunks = TextChunker.chunk(text, paragraph.length() + 10);
        assertThat(chunks).hasSizeGreaterThan(1);
        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(paragraph.length() + 10 + 4);
        }
    }

    @Test
    void hardSplitsAParagraphLongerThanTheLimit() {
        String hugeParagraph = "x".repeat(250);
        List<String> chunks = TextChunker.chunk(hugeParagraph, 100);
        assertThat(chunks).hasSize(3);
        assertThat(String.join("", chunks)).hasSize(250);
    }
}
