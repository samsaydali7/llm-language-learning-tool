package com.languagelearning.audio.entity;

import com.languagelearning.book.entity.Book;
import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.structure.entity.StructureNode;
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
 * An in-text marker such as "Track 12" or "CD1-05" found while scanning a book's transcript, and
 * (when possible) the {@link AudioFile} it has been matched to (REQUIREMENTS.md - "connect audio
 * files with the appropriate parts of the book whenever possible"). Left unmatched when no
 * uploaded file corresponds, so the user can link it manually.
 */
@Entity
@Table(name = "audio_reference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioReference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_node_id")
    private StructureNode structureNode;

    @Column(name = "page")
    private Integer page;

    /** The raw marker text found in the PDF, e.g. "Track 12". */
    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "raw_context", columnDefinition = "text")
    private String rawContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_file_id")
    private AudioFile audioFile;

    @Column(name = "match_confidence")
    private Double matchConfidence;
}
