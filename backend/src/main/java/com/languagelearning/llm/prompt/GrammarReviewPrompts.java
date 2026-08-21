package com.languagelearning.llm.prompt;

import com.languagelearning.llm.dto.GrammarReviewRequest;

/** Builds the instruction + JSON-schema prompt sent to the grammar-review model. */
public final class GrammarReviewPrompts {

    private GrammarReviewPrompts() {
    }

    public static String build(GrammarReviewRequest request) {
        String examples = request.examples() == null || request.examples().isEmpty()
                ? "(none captured)" : String.join("\n", request.examples());
        String failures = request.failureNotes() == null || request.failureNotes().isEmpty()
                ? "(no recorded mistakes)" : String.join("\n", request.failureNotes());

        return """
                You are a %s grammar tutor writing a review, in %s, for a learner studying "%s".

                Original explanation from the book:
                %s

                Examples from the book:
                %s

                The learner has recently struggled with:
                %s

                Write a focused review that re-explains the rule clearly, calls out the mistakes the \
                learner is actually making, and gives a few fresh reinforcement examples (not copied \
                verbatim from the book).

                Respond with ONLY a single JSON object of this shape:
                {
                  "summary": "a clear restatement of the rule",
                  "keyPoints": ["short bullet points"],
                  "commonMistakes": ["mistakes learners like this one tend to make, tailored to the failure notes above"],
                  "reinforcementExamples": [{"text": "new example sentence", "translation": "translation in %s"}]
                }
                """.formatted(
                request.learningLanguageName(),
                request.explanationLanguageName(),
                request.grammarTitle(),
                request.explanation(),
                examples,
                failures,
                request.explanationLanguageName());
    }
}
