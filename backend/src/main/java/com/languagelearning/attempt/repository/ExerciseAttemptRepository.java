package com.languagelearning.attempt.repository;

import com.languagelearning.attempt.entity.ExerciseAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {

    List<ExerciseAttempt> findByExerciseIdOrderByAttemptedAtDesc(Long exerciseId);
}
