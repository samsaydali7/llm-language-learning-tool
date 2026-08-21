package com.languagelearning.job.api;

import com.languagelearning.job.entity.ExerciseGenerationJob;
import com.languagelearning.job.entity.ExtractionJob;

public class JobDtos {

    private JobDtos() {
    }

    public record ExtractionJobResponse(
            Long id,
            Long bookId,
            String status,
            String stage,
            Integer totalSections,
            Integer completedSections,
            String errorMessage) {

        public static ExtractionJobResponse from(ExtractionJob job) {
            return new ExtractionJobResponse(
                    job.getId(),
                    job.getBook().getId(),
                    job.getStatus().name(),
                    job.getStage().name(),
                    job.getTotalSections(),
                    job.getCompletedSections(),
                    job.getErrorMessage());
        }
    }

    public record ExerciseGenerationJobResponse(
            Long id,
            String status,
            Integer exerciseCount,
            String errorMessage) {

        public static ExerciseGenerationJobResponse from(ExerciseGenerationJob job) {
            return new ExerciseGenerationJobResponse(
                    job.getId(), job.getStatus().name(), job.getExerciseCount(), job.getErrorMessage());
        }
    }
}
