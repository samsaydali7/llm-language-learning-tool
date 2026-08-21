package com.languagelearning.extraction;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a section's text into chunks small enough to fit the extraction model's context window
 * (SPEC.md #5 - the app must run on a 16GB machine with small quantized models), preferring to
 * break on paragraph/line boundaries so a sentence is never split mid-way when avoidable.
 */
public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> chunk(String text, int maxChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (current.length() + trimmed.length() + 2 > maxChars && !current.isEmpty()) {
                chunks.add(current.toString().strip());
                current = new StringBuilder();
            }
            if (trimmed.length() > maxChars) {
                // A single paragraph longer than the limit: hard-split it.
                for (int i = 0; i < trimmed.length(); i += maxChars) {
                    chunks.add(trimmed.substring(i, Math.min(i + maxChars, trimmed.length())));
                }
                continue;
            }
            current.append(trimmed).append("\n\n");
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return chunks;
    }
}
