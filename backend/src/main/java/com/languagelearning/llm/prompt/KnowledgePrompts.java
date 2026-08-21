package com.languagelearning.llm.prompt;

import com.languagelearning.llm.dto.KnowledgeExtractionRequest;

/** Builds the instruction + JSON-schema prompt sent to the extraction model. */
public final class KnowledgePrompts {

    private KnowledgePrompts() {
    }

    public static String build(KnowledgeExtractionRequest request) {
        return """
                You are a language-learning content analyst. You read a page from a %s textbook \
                and extract structured study material. Explanations, meanings, and notes must be \
                written in %s. Do not invent content that is not supported by the source text.

                Book: %s
                Section: %s
                %s

                Source text:
                ---
                %s
                ---

                Respond with ONLY a single JSON object matching exactly this shape (omit nothing, \
                use empty arrays [] when a category has nothing relevant):
                {
                  "vocabulary": [
                    {"headword": "", "partOfSpeech": "", "meaning": "", "notes": "",
                     "examples": [{"text": "", "translation": ""}]}
                  ],
                  "grammar": [
                    {"title": "", "explanation": "", "patterns": "",
                     "examples": [{"text": "", "translation": ""}]}
                  ],
                  "expressions": [
                    {"phrase": "", "meaning": "", "usageNotes": "",
                     "examples": [{"text": "", "translation": ""}]}
                  ],
                  "topics": ["short topic names such as Restaurant, Travel, Greetings - derive these \
                from the content, do not use a fixed list"]
                }
                """.formatted(
                request.learningLanguageName(),
                request.explanationLanguageName(),
                request.bookTitle(),
                request.sectionTitle(),
                request.page() != null ? "Page: " + request.page() : "",
                request.sourceText());
    }
}
