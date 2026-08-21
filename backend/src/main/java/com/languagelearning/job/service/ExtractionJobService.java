package com.languagelearning.job.service;

import com.languagelearning.book.entity.Book;
import com.languagelearning.common.exception.InvalidRequestException;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.job.entity.ExtractionJob;
import com.languagelearning.job.entity.ExtractionStage;
import com.languagelearning.job.entity.JobStatus;
import com.languagelearning.job.messaging.JobPublisher;
import com.languagelearning.job.messaging.KnowledgeExtractionMessage;
import com.languagelearning.job.messaging.StructureExtractionMessage;
import com.languagelearning.job.repository.ExtractionJobRepository;
import com.languagelearning.knowledge.repository.KnowledgeItemRepository;
import com.languagelearning.structure.KnowledgeCoverageRange;
import com.languagelearning.structure.service.StructureNodeService;
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
    private final StructureNodeService structureNodeService;
    private final KnowledgeItemRepository knowledgeItemRepository;

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

    /**
     * Re-plans and re-publishes knowledge-extraction work against a book's already-detected
     * structure, without touching the PDF or re-running structure detection. For recovering a job
     * whose queued messages were lost (e.g. to a RabbitMQ restart) - the structure/audio data
     * already persisted is untouched, only knowledge items are cleared first so a retry can't
     * produce duplicates alongside whatever had already completed.
     */
    public ExtractionJob retryKnowledgeExtraction(Book book) {
        if (book.getPdfPath() == null || book.getPdfPageCount() == null) {
            throw new InvalidRequestException("Book " + book.getId() + " has no PDF uploaded yet");
        }
        List<KnowledgeCoverageRange> ranges =
                structureNodeService.planKnowledgeExtractionRanges(book.getId(), book.getPdfPageCount());
        if (ranges.isEmpty()) {
            throw new InvalidRequestException(
                    "Book " + book.getId() + " has no detected structure to extract from - upload its PDF again");
        }

        knowledgeItemRepository.deleteByBookId(book.getId());

        ExtractionJob job = extractionJobRepository.save(ExtractionJob.builder()
                .book(book)
                .status(JobStatus.RUNNING)
                .stage(ExtractionStage.KNOWLEDGE)
                .totalSections(ranges.size())
                .completedSections(0)
                .build());
        for (KnowledgeCoverageRange range : ranges) {
            jobPublisher.publishKnowledgeExtraction(
                    new KnowledgeExtractionMessage(job.getId(), book.getId(), range.structureNodeId(), range.startPage(), range.endPage()));
        }
        log.info("Retrying knowledge extraction for book {}: {} message(s) re-published as job {}",
                book.getId(), ranges.size(), job.getId());
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
