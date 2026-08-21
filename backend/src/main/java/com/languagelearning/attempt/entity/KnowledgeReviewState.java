package com.languagelearning.attempt.entity;

import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A simple per-{@link KnowledgeItem} failure counter (REQUIREMENTS.md - "Review and retention";
 * SPEC.md non-goals exclude advanced spaced repetition, so this is deliberately just a counter
 * used to prioritize review/grammar-review ordering, not a scheduling algorithm).
 */
@Entity
@Table(name = "knowledge_review_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeReviewState extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_item_id", nullable = false, unique = true)
    private KnowledgeItem knowledgeItem;

    @Column(name = "times_failed", nullable = false)
    @Builder.Default
    private Integer timesFailed = 0;

    @Column(name = "times_reviewed", nullable = false)
    @Builder.Default
    private Integer timesReviewed = 0;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;
}
