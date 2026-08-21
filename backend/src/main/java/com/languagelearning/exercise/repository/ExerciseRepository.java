package com.languagelearning.exercise.repository;

import com.languagelearning.exercise.entity.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByJobId(Long jobId);
}
