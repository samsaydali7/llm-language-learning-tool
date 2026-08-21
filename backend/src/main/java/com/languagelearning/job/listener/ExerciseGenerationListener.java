package com.languagelearning.job.listener;

import com.languagelearning.exercise.ExerciseGenerationParams;
import com.languagelearning.exercise.ExerciseGenerationService;
import com.languagelearning.job.entity.ExerciseGenerationJob;
import com.languagelearning.job.messaging.ExerciseGenerationMessage;
import com.languagelearning.job.messaging.RabbitConfig;
import com.languagelearning.job.service.ExerciseGenerationJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Handles job-based exercise generation (SPEC.md #2.1). */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExerciseGenerationListener {

    private final ExerciseGenerationJobService jobService;
    private final ExerciseGenerationService exerciseGenerationService;

    @RabbitListener(queues = RabbitConfig.EXERCISE_GENERATION_QUEUE)
    public void onMessage(ExerciseGenerationMessage message) {
        try {
            jobService.markRunning(message.jobId());
            ExerciseGenerationJob job = jobService.getById(message.jobId());
            var scope = jobService.readScope(job);
            var types = jobService.readTypes(job);
            var exercises = exerciseGenerationService.generate(
                    ExerciseGenerationParams.forJob(job, scope, job.getExerciseCount(), types));
            jobService.markCompleted(message.jobId());
            log.info("Exercise generation job {} produced {} exercise(s)", message.jobId(), exercises.size());
        } catch (Exception e) {
            log.error("Exercise generation job {} failed", message.jobId(), e);
            jobService.markFailed(message.jobId(), e.getMessage());
        }
    }
}
