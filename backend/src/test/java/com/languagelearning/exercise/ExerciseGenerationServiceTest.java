package com.languagelearning.exercise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.book.entity.Book;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.exercise.repository.ExerciseRepository;
import com.languagelearning.knowledge.entity.VocabularyItem;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.language.entity.Language;
import com.languagelearning.llm.LlmRouter;
import com.languagelearning.llm.dto.ExerciseGenerationResult;
import com.languagelearning.llm.dto.ExerciseGenerationResult.GeneratedExercise;
import com.languagelearning.scope.KnowledgeQueryService;
import com.languagelearning.scope.StudyScope;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExerciseGenerationServiceTest {

    private KnowledgeQueryService knowledgeQueryService;
    private KnowledgeExampleRepository knowledgeExampleRepository;
    private LlmRouter llmRouter;
    private ExerciseRepository exerciseRepository;
    private ExerciseGenerationService service;
    private VocabularyItem item;

    @BeforeEach
    void setUp() throws Exception {
        knowledgeQueryService = mock(KnowledgeQueryService.class);
        knowledgeExampleRepository = mock(KnowledgeExampleRepository.class);
        llmRouter = mock(LlmRouter.class);
        exerciseRepository = mock(ExerciseRepository.class);
        service = new ExerciseGenerationService(knowledgeQueryService, knowledgeExampleRepository, llmRouter, exerciseRepository);

        Language language = Language.builder().code("fr").name("French").build();
        Book book = Book.builder().language(language).title("French for Dummies").explanationLanguageCode("en").build();
        item = VocabularyItem.builder().book(book).headword("réservation").summary("reservation").build();
        setId(item, 42L);

        when(knowledgeQueryService.resolve(any())).thenReturn(List.of(item));
        when(knowledgeExampleRepository.findByKnowledgeItemIdIn(any())).thenReturn(List.of());
    }

    @Test
    void doesNotPersistWhenPersistIsFalse() {
        when(llmRouter.generateExercises(any())).thenReturn(new ExerciseGenerationResult(List.of(
                new GeneratedExercise("MULTIPLE_CHOICE", "What does 'réservation' mean?",
                        List.of("reservation", "menu"), "reservation", "It's a cognate.", List.of(42L)))));

        List<Exercise> exercises = service.generate(ExerciseGenerationParams.onDemand(
                new StudyScope(null, 1L, List.of(), List.of(), List.of()), 1, List.of(), false));

        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getSourceKnowledgeItems()).containsExactly(item);
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void persistsWhenPersistIsTrue() {
        when(llmRouter.generateExercises(any())).thenReturn(new ExerciseGenerationResult(List.of(
                new GeneratedExercise("TRANSLATION", "Translate 'réservation'", List.of(), "reservation", null, List.of(42L)))));
        when(exerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Exercise> exercises = service.generate(ExerciseGenerationParams.onDemand(
                new StudyScope(null, 1L, List.of(), List.of(), List.of()), 1, List.of(), true));

        assertThat(exercises).hasSize(1);
        verify(exerciseRepository).save(any());
    }

    @Test
    void skipsGeneratedItemsMissingARequiredField() {
        when(llmRouter.generateExercises(any())).thenReturn(new ExerciseGenerationResult(List.of(
                new GeneratedExercise("TRANSLATION", "", List.of(), "", null, List.of()))));

        List<Exercise> exercises = service.generate(ExerciseGenerationParams.onDemand(
                new StudyScope(null, 1L, List.of(), List.of(), List.of()), 1, List.of(ExerciseType.TRANSLATION), false));

        assertThat(exercises).isEmpty();
    }

    @Test
    void resolvesA1BasedOptionIndexToItsTextWhenTheModelAnswersWithAnIndex() {
        when(llmRouter.generateExercises(any())).thenReturn(new ExerciseGenerationResult(List.of(
                new GeneratedExercise("MULTIPLE_CHOICE", "Which word means 'reservation'?",
                        List.of("menu", "réservation", "addition"), "2", null, List.of(42L)))));

        List<Exercise> exercises = service.generate(ExerciseGenerationParams.onDemand(
                new StudyScope(null, 1L, List.of(), List.of(), List.of()), 1, List.of(ExerciseType.MULTIPLE_CHOICE), false));

        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getCorrectAnswer()).isEqualTo("réservation");
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = findField(entity.getClass(), "id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
