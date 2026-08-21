package com.languagelearning.job.service;

import com.languagelearning.book.entity.Book;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.job.entity.ExtractionJob;
import com.languagelearning.job.entity.ExtractionStage;
import com.languagelearning.job.entity.JobStatus;
import com.languagelearning.job.messaging.JobPublisher;
import com.languagelearning.job.messaging.StructureExtractionMessage;
import com.languagelearning.job.repository.ExtractionJobRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExtractionJobService {

    private final ExtractionJobRepository extractionJobRepository;
    private final JobPublisher jobPublisher;

    public ExtractionJob startForBook(Book book) {
        ExtractionJob job = extractionJobRepository.save(ExtractionJob.builder()
                .book(book)
                .status(JobStatus.PENDING)
                .stage(ExtractionStage.STRUCTURE)
                .completedSections(0)
                .build());
        jobPublisher.publishStructureExtraction(new StructureExtractionMessage(job.getId(), book.getId()));
        return job;
    }

    public ExtractionJob getById(Long id) {
        return extractionJobRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ExtractionJob", id));
    }

    @Transactional(readOnly = true)
    public List<ExtractionJob> findByBook(Long bookId) {
        return extractionJobRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public void markRunning(Long jobId) {
        ExtractionJob job = getById(jobId);
        job.setStatus(JobStatus.RUNNING);
        extractionJobRepository.save(job);
    }

    public void advanceToKnowledgeStage(Long jobId, int totalSections) {
        ExtractionJob job = getById(jobId);
        job.setStage(ExtractionStage.KNOWLEDGE);
        job.setTotalSections(totalSections);
        job.setStatus(totalSections == 0 ? JobStatus.COMPLETED : JobStatus.RUNNING);
        extractionJobRepository.save(job);
    }

    public synchronized void recordSectionCompleted(Long jobId) {
        ExtractionJob job = getById(jobId);
        int completed = (job.getCompletedSections() == null ? 0 : job.getCompletedSections()) + 1;
        job.setCompletedSections(completed);
        if (job.getTotalSections() != null && completed >= job.getTotalSections()) {
            job.setStatus(JobStatus.COMPLETED);
        }
        extractionJobRepository.save(job);
    }

    public void markFailed(Long jobId, String message) {
        ExtractionJob job = getById(jobId);
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(message);
        extractionJobRepository.save(job);
        log.error("Extraction job {} failed: {}", jobId, message);
    }
}
