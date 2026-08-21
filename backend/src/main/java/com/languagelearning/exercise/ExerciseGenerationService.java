package com.languagelearning.exercise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.languagelearning.book.entity.Book;
import com.languagelearning.common.LanguageNames;
import com.languagelearning.common.exception.InvalidRequestException;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.exercise.repository.ExerciseRepository;
import com.languagelearning.knowledge.entity.KnowledgeExample;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.llm.LlmRouter;
import com.languagelearning.llm.dto.ExerciseGenerationRequest;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.ExerciseGenerationResult.GeneratedExercise;
import com.languagelearning.llm.dto.KnowledgeSnippet;
import com.languagelearning.scope.KnowledgeQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The single generation pipeline shared by job-based and on-demand exercise generation
 * (SPEC.md #2.3): resolve scope -> build LLM request -> call {@link LlmRouter} -> persist (or
 * not, per {@link ExerciseGenerationParams#persist()}), always keeping generated exercises linked
 * back to the knowledge items and book they were generated from.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseGenerationService {

    private final KnowledgeQueryService knowledgeQueryService;
    private final KnowledgeExampleRepository knowledgeExampleRepository;
    private final LlmRouter llmRouter;
    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Exercise> generate(ExerciseGenerationParams params) {
        List<KnowledgeItem> items = knowledgeQueryService.resolve(params.scope());
        if (items.isEmpty() && params.listeningContext() == null) {
            throw new InvalidRequestException("No knowledge items match the given study scope");
        }

        Book referenceBook = !items.isEmpty() ? items.get(0).getBook() : null;
        String learningLanguage = referenceBook != null ? referenceBook.getLanguage().getName() : "the target language";
        String explanationLanguage = referenceBook != null
                ? LanguageNames.displayName(referenceBook.getExplanationLanguageCode()) : "English";

        Map<Long, KnowledgeExample> exampleByItem = exampleByItem(items);
        List<KnowledgeSnippet> snippets = items.stream()
                .map(item -> toSnippet(item, exampleByItem.get(item.getId())))
                .toList();

        List<String> typeNames = (params.allowedTypes() == null || params.allowedTypes().isEmpty())
                ? Arrays.stream(ExerciseType.values())
                        .filter(t -> t != ExerciseType.LISTENING_COMPREHENSION)
                        .map(Enum::name).toList()
                : params.allowedTypes().stream().map(Enum::name).toList();

        ExerciseGenerationRequest request = new ExerciseGenerationRequest(
                learningLanguage, explanationLanguage, typeNames, params.count(), snippets, params.listeningContext(), null);
        ExerciseGenerationResult result = llmRouter.generateExercises(request);

        Map<Long, KnowledgeItem> itemsById = items.stream().collect(Collectors.toMap(KnowledgeItem::getId, i -> i));

        List<Exercise> exercises = new ArrayList<>();
        for (GeneratedExercise generated : result.exercises()) {
            ExerciseType type = parseType(generated.type());
            if (type == null || generated.prompt() == null || generated.prompt().isBlank()
                    || generated.correctAnswer() == null || generated.correctAnswer().isBlank()) {
                continue;
            }
            Set<KnowledgeItem> sources = generated.sourceKnowledgeItemIds().stream()
                    .map(itemsById::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            Exercise exercise = Exercise.builder()
                    .job(params.job())
                    .book(referenceBook != null ? referenceBook : (params.audioFile() != null ? params.audioFile().getBook() : null))
                    .type(type)
                    .prompt(generated.prompt())
                    .optionsJson(writeJson(generated.options()))
                    .correctAnswer(resolveCorrectAnswer(generated.correctAnswer(), generated.options()))
                    .explanation(generated.explanation())
                    .audioFile(params.audioFile())
                    .sourceKnowledgeItems(sources)
                    .build();

            exercises.add(params.persist() ? exerciseRepository.save(exercise) : exercise);
        }
        return exercises;
    }

    /**
     * Small local models generating multiple-choice/matching items occasionally answer with the
     * option's position ("2") rather than its text, even when the prompt asks for the text
     * (SPEC.md #5 - small quantized models). If the raw answer isn't one of the options verbatim,
     * try reading it as a 1-based (falling back to 0-based) index into them before giving up and
     * keeping the raw value as-is.
     */
    private String resolveCorrectAnswer(String rawAnswer, List<String> options) {
        if (rawAnswer == null || options == null || options.isEmpty()) {
            return rawAnswer;
        }
        boolean matchesAnOption = options.stream().anyMatch(o -> o.equalsIgnoreCase(rawAnswer.strip()));
        if (matchesAnOption) {
            return rawAnswer;
        }
        try {
            int index = Integer.parseInt(rawAnswer.strip());
            if (index >= 1 && index <= options.size()) {
                return options.get(index - 1);
            }
            if (index >= 0 && index < options.size()) {
                return options.get(index);
            }
        } catch (NumberFormatException ignored) {
            // Not an index; keep the model's raw text answer.
        }
        return rawAnswer;
    }

    private Map<Long, KnowledgeExample> exampleByItem(List<KnowledgeItem> items) {
        List<Long> ids = items.stream().map(KnowledgeItem::getId).toList();
        Map<Long, KnowledgeExample> map = new HashMap<>();
        for (KnowledgeExample example : knowledgeExampleRepository.findByKnowledgeItemIdIn(ids)) {
            map.putIfAbsent(example.getKnowledgeItem().getId(), example);
        }
        return map;
    }

    private KnowledgeSnippet toSnippet(KnowledgeItem item, KnowledgeExample example) {
        return new KnowledgeSnippet(
                item.getId(),
                item.getType().name(),
                item.getHeadword(),
                item.getSummary(),
                example != null ? example.getExampleText() : null);
    }

    private ExerciseType parseType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return ExerciseType.valueOf(raw.trim().toUpperCase().replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String writeJson(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options == null ? List.of() : options);
        } catch (Exception e) {
            return "[]";
        }
    }
}
