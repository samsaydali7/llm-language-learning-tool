package com.languagelearning.job.repository;

import com.languagelearning.job.entity.ExtractionJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractionJobRepository extends JpaRepository<ExtractionJob, Long> {

    List<ExtractionJob> findByBookIdOrderByCreatedAtDesc(Long bookId);
}
