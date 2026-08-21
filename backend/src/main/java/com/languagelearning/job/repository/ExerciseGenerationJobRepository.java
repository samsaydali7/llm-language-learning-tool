package com.languagelearning.job.repository;

import com.languagelearning.job.entity.ExerciseGenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseGenerationJobRepository extends JpaRepository<ExerciseGenerationJob, Long> {
}
