package com.languagelearning.exercise;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.job.entity.ExerciseGenerationJob;
import com.languagelearning.scope.StudyScope;
import java.util.List;

/**
 * Parameters for {@link ExerciseGenerationService#generate}, covering both the job-based and
 * on-demand modes (SPEC.md #2) plus the listening variant (which supplies transcript context and
 * the source audio file instead of relying purely on knowledge items).
 */
public record ExerciseGenerationParams(
        StudyScope scope,
        int count,
        List<ExerciseType> allowedTypes,
        boolean persist,
        ExerciseGenerationJob job,
        String listeningContext,
        AudioFile audioFile) {

    public static ExerciseGenerationParams onDemand(StudyScope scope, int count, List<ExerciseType> types, boolean persist) {
        return new ExerciseGenerationParams(scope, count, types, persist, null, null, null);
    }

    public static ExerciseGenerationParams forJob(ExerciseGenerationJob job, StudyScope scope, int count, List<ExerciseType> types) {
        return new ExerciseGenerationParams(scope, count, types, true, job, null, null);
    }
}
