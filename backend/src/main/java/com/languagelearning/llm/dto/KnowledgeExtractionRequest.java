package com.languagelearning.llm.dto;

/**
 * A chunk of book text to extract knowledge from.
 *
 * @param learningLanguageName    e.g. "French" - the language being learned (no business logic is
 *                                specific to this value; it is just passed through to the prompt)
 * @param explanationLanguageName e.g. "English" - the language meanings/notes should be written in
 * @param bookTitle               used only to give the model orientation, not persisted from here
 * @param sectionTitle            title of the structural node this chunk belongs to
 * @param sourceText              the raw text chunk extracted from the PDF
 * @param page                    the page the chunk starts on, for provenance hints in the prompt
 * @param model                   the concrete model to invoke, set by {@link com.languagelearning.llm.LlmRouter}
 */
public record KnowledgeExtractionRequest(
        String learningLanguageName,
        String explanationLanguageName,
        String bookTitle,
        String sectionTitle,
        String sourceText,
        Integer page,
        String model) {

    public KnowledgeExtractionRequest withModel(String newModel) {
        return new KnowledgeExtractionRequest(
                learningLanguageName, explanationLanguageName, bookTitle, sectionTitle, sourceText, page, newModel);
    }
}
