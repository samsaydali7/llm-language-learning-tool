package com.languagelearning.llm.prompt;

import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.KnowledgeSnippet;
import java.util.stream.Collectors;

/** Builds the instruction + JSON-schema prompt sent to the exercise-generation model. */
public final class ExercisePrompts {

    private ExercisePrompts() {
    }

    public static String build(ExerciseGenerationRequest request) {
        String knowledgeBlock = request.knowledge().stream()
                .map(ExercisePrompts::renderSnippet)
                .collect(Collectors.joining("\n"));
        String listeningBlock = (request.listeningContext() == null || request.listeningContext().isBlank())
                ? ""
                : "\nAudio transcript to use for listening-style items:\n---\n" + request.listeningContext() + "\n---\n";

        return """
                You are a language-learning exercise author. Create exactly %d exercises in %s for a \
                learner of %s, using ONLY the knowledge items and transcript given below - do not \
                introduce vocabulary or grammar that isn't listed. Choose exercise types only from: %s.

                Knowledge items (id: type "headword" = meaning | example):
                %s
                %s

                Respond with ONLY a single JSON object of this shape:
                {
                  "exercises": [
                    {
                      "type": "one of the allowed types",
                      "prompt": "the exercise text shown to the learner",
                      "options": ["for multiple choice / matching; [] otherwise"],
                      "correctAnswer": "the correct answer",
                      "explanation": "short explanation of why, in %s",
                      "sourceKnowledgeItemIds": [the integer ids of the knowledge items this exercise draws from]
                    }
                  ]
                }
                """.formatted(
                request.count(),
                request.explanationLanguageName(),
                request.learningLanguageName(),
                String.join(", ", request.exerciseTypes()),
                knowledgeBlock.isBlank() ? "(none - rely on the transcript below)" : knowledgeBlock,
                listeningBlock,
                request.explanationLanguageName());
    }

    private static String renderSnippet(KnowledgeSnippet snippet) {
        return "%d: %s \"%s\" = %s%s".formatted(
                snippet.referenceId(),
                snippet.type(),
                snippet.headword(),
                snippet.meaning(),
                snippet.exampleText() == null || snippet.exampleText().isBlank()
                        ? "" : " | e.g. " + snippet.exampleText());
    }
}
