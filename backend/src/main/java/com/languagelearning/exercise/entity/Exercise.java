package com.languagelearning.exercise.entity;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.book.entity.Book;
import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.job.entity.ExerciseGenerationJob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single generated exercise. Every exercise carries a correct answer (REQUIREMENTS.md -
 * "Exercise Answers") and stays linked to the knowledge it was generated from, so attempts can
 * feed failures back to specific vocabulary/grammar/expressions (REQUIREMENTS.md - "Review and
 * retention"). {@code job} is null for exercises generated on-demand without persistence choosing
 * to be saved standalone (SPEC.md #2.2).
 */
@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private ExerciseGenerationJob job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 32)
    private ExerciseType type;

    @Column(name = "prompt", nullable = false, columnDefinition = "text")
    private String prompt;

    /** JSON array of option strings; used by MULTIPLE_CHOICE / MATCHING, empty otherwise. */
    @Column(name = "options_json", columnDefinition = "text")
    private String optionsJson;

    @Column(name = "correct_answer", nullable = false, columnDefinition = "text")
    private String correctAnswer;

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_file_id")
    private AudioFile audioFile;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exercise_knowledge_item",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "knowledge_item_id"))
    @Builder.Default
    private Set<KnowledgeItem> sourceKnowledgeItems = new HashSet<>();
}
