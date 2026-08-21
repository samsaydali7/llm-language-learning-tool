package com.languagelearning.job.entity;

import com.languagelearning.book.entity.Book;
import com.languagelearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Tracks the async pipeline (structure extraction -> per-section knowledge extraction) for one
 * book's PDF upload (SPEC.md #2/#3 - persistent, job-based processing).
 */
@Entity
@Table(name = "extraction_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionJob extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 32)
    private ExtractionStage stage;

    @Column(name = "total_sections")
    private Integer totalSections;

    @Column(name = "completed_sections")
    @Builder.Default
    private Integer completedSections = 0;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
