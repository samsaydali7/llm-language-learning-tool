package com.languagelearning.book.entity;

import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.language.entity.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A learning corpus/book: a PDF plus its associated audio, scoped to a single {@link Language}.
 * The book and its audio are treated as parts of the same learning source (REQUIREMENTS.md).
 */
@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    // Eager: almost every consumer of a Book (DTO mapping, prompt building, RabbitMQ listener
    // threads that fall outside the web request's open-in-view session) needs the language name
    // or code, and Language is a tiny, cheap lookup table, so there is no real cost to always
    // loading it with its Book.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Language the explanations/instructions should be presented in, e.g. "en". */
    @Column(name = "explanation_language_code", nullable = false, length = 16)
    private String explanationLanguageCode;

    /** Path (relative to storage base path) to the uploaded PDF, null until uploaded. */
    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "pdf_page_count")
    private Integer pdfPageCount;
}
