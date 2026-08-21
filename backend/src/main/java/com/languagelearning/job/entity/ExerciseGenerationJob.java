package com.languagelearning.job.entity;

import com.languagelearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A persistent, job-based exercise-generation request over a {@code StudyScope} (SPEC.md #2.1).
 * The scope and requested exercise types are stored as JSON since they are an arbitrary
 * combination of filters, not a fixed shape.
 */
@Entity
@Table(name = "exercise_generation_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseGenerationJob extends BaseEntity {

    @Column(name = "scope_json", nullable = false, columnDefinition = "text")
    private String scopeJson;

    @Column(name = "exercise_types_json", columnDefinition = "text")
    private String exerciseTypesJson;

    @Column(name = "exercise_count", nullable = false)
    private Integer exerciseCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
