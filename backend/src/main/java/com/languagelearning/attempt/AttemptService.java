package com.languagelearning.attempt;

import com.languagelearning.attempt.entity.ExerciseAttempt;
import com.languagelearning.attempt.entity.KnowledgeReviewState;
import com.languagelearning.attempt.repository.ExerciseAttemptRepository;
import com.languagelearning.attempt.repository.KnowledgeReviewStateRepository;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.repository.ExerciseRepository;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grades a submitted answer and, on failure, links the mistake back to every knowledge item the
 * exercise was generated from (REQUIREMENTS.md - "Review and retention": "Failed material is
 * connected back to vocabulary, grammar, and topic metadata so it can be revisited").
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttemptService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseAttemptRepository attemptRepository;
    private final KnowledgeReviewStateRepository reviewStateRepository;

    public AttemptResult submit(Long exerciseId, String answer) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Exercise", exerciseId));

        boolean correct = isCorrect(answer, exercise.getCorrectAnswer());

        attemptRepository.save(ExerciseAttempt.builder()
                .exercise(exercise)
                .submittedAnswer(answer)
                .correct(correct)
                .attemptedAt(Instant.now())
                .build());

        if (!correct) {
            for (KnowledgeItem item : exercise.getSourceKnowledgeItems()) {
                recordFailure(item);
            }
        }
        return new AttemptResult(correct, exercise.getCorrectAnswer(), exercise.getExplanation());
    }

    @Transactional(readOnly = true)
    public List<ExerciseAttempt> history(Long exerciseId) {
        return attemptRepository.findByExerciseIdOrderByAttemptedAtDesc(exerciseId);
    }

    private void recordFailure(KnowledgeItem item) {
        KnowledgeReviewState state = reviewStateRepository.findByKnowledgeItemId(item.getId())
                .orElseGet(() -> KnowledgeReviewState.builder().knowledgeItem(item).timesFailed(0).timesReviewed(0).build());
        state.setTimesFailed(state.getTimesFailed() + 1);
        state.setLastFailedAt(Instant.now());
        reviewStateRepository.save(state);
    }

    private boolean isCorrect(String submitted, String expected) {
        if (submitted == null || expected == null) {
            return false;
        }
        return normalize(submitted).equals(normalize(expected));
    }

    private String normalize(String s) {
        return s.strip().toLowerCase().replaceAll("\\s+", " ").replaceAll("[.!?,;:]+$", "");
    }

    public record AttemptResult(boolean correct, String correctAnswer, String explanation) {
    }
}
