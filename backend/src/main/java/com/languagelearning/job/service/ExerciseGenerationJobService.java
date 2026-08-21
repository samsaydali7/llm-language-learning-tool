package com.languagelearning.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.job.entity.ExerciseGenerationJob;
import com.languagelearning.job.entity.JobStatus;
import com.languagelearning.job.messaging.ExerciseGenerationMessage;
import com.languagelearning.job.messaging.JobPublisher;
import com.languagelearning.job.repository.ExerciseGenerationJobRepository;
import com.languagelearning.scope.StudyScope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExerciseGenerationJobService {

    private final ExerciseGenerationJobRepository repository;
    private final JobPublisher jobPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExerciseGenerationJob create(StudyScope scope, int count, List<ExerciseType> types) {
        ExerciseGenerationJob job = ExerciseGenerationJob.builder()
                .scopeJson(writeJson(scope))
                .exerciseTypesJson(writeJson(types))
                .exerciseCount(count)
                .status(JobStatus.PENDING)
                .build();
        job = repository.save(job);
        jobPublisher.publishExerciseGeneration(new ExerciseGenerationMessage(job.getId()));
        return job;
    }

    public ExerciseGenerationJob getById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ExerciseGenerationJob", id));
    }

    public StudyScope readScope(ExerciseGenerationJob job) {
        return readJson(job.getScopeJson(), StudyScope.class);
    }

    public List<ExerciseType> readTypes(ExerciseGenerationJob job) {
        if (job.getExerciseTypesJson() == null) {
            return List.of();
        }
        ExerciseType[] types = readJson(job.getExerciseTypesJson(), ExerciseType[].class);
        return List.of(types);
    }

    public void markRunning(Long jobId) {
        ExerciseGenerationJob job = getById(jobId);
        job.setStatus(JobStatus.RUNNING);
        repository.save(job);
    }

    public void markCompleted(Long jobId) {
        ExerciseGenerationJob job = getById(jobId);
        job.setStatus(JobStatus.COMPLETED);
        repository.save(job);
    }

    public void markFailed(Long jobId, String message) {
        ExerciseGenerationJob job = getById(jobId);
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(message);
        repository.save(job);
        log.error("Exercise generation job {} failed: {}", jobId, message);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize job payload", e);
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize job payload", e);
        }
    }
}
