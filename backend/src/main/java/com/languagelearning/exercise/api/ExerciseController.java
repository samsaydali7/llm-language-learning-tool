package com.languagelearning.exercise.api;

import com.languagelearning.attempt.AttemptService;
import com.languagelearning.attempt.AttemptService.AttemptResult;
import com.languagelearning.exercise.ExerciseGenerationParams;
import com.languagelearning.exercise.ExerciseGenerationService;
import com.languagelearning.exercise.api.ExerciseDtos.AttemptResultResponse;
import com.languagelearning.exercise.api.ExerciseDtos.CreateExerciseJobRequest;
import com.languagelearning.exercise.api.ExerciseDtos.ExerciseResponse;
import com.languagelearning.exercise.api.ExerciseDtos.GenerateExercisesRequest;
import com.languagelearning.exercise.api.ExerciseDtos.SubmitAttemptRequest;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.repository.ExerciseRepository;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.job.api.JobDtos.ExerciseGenerationJobResponse;
import com.languagelearning.job.entity.ExerciseGenerationJob;
import com.languagelearning.job.service.ExerciseGenerationJobService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseGenerationService exerciseGenerationService;
    private final ExerciseGenerationJobService exerciseGenerationJobService;
    private final ExerciseRepository exerciseRepository;
    private final AttemptService attemptService;

    /** On-demand generation (SPEC.md #2.2): fast, narrowly-scoped, optionally persisted. */
    @PostMapping("/api/exercises/generate")
    public List<ExerciseResponse> generateOnDemand(@Valid @RequestBody GenerateExercisesRequest request) {
        List<Exercise> exercises = exerciseGenerationService.generate(ExerciseGenerationParams.onDemand(
                request.scope().toScope(), request.count(), request.exerciseTypes(), request.persist()));
        return exercises.stream().map(ExerciseResponse::from).toList();
    }

    @GetMapping("/api/exercises/{id}")
    public ExerciseResponse get(@PathVariable Long id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Exercise", id));
        return ExerciseResponse.from(exercise);
    }

    @PostMapping("/api/exercises/{id}/attempts")
    public AttemptResultResponse submitAttempt(@PathVariable Long id, @RequestBody SubmitAttemptRequest request) {
        AttemptResult result = attemptService.submit(id, request.answer());
        return new AttemptResultResponse(result.correct(), result.correctAnswer(), result.explanation());
    }

    /** Persistent, job-based generation (SPEC.md #2.1). */
    @PostMapping("/api/exercise-jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseGenerationJobResponse createJob(@Valid @RequestBody CreateExerciseJobRequest request) {
        ExerciseGenerationJob job = exerciseGenerationJobService.create(
                request.scope().toScope(), request.count(), request.exerciseTypes());
        return ExerciseGenerationJobResponse.from(job);
    }

    @GetMapping("/api/exercise-jobs/{id}")
    public ExerciseGenerationJobResponse getJob(@PathVariable Long id) {
        return ExerciseGenerationJobResponse.from(exerciseGenerationJobService.getById(id));
    }

    @GetMapping("/api/exercise-jobs/{id}/exercises")
    public List<ExerciseResponse> jobExercises(@PathVariable Long id) {
        return exerciseRepository.findByJobId(id).stream().map(ExerciseResponse::from).toList();
    }
}
