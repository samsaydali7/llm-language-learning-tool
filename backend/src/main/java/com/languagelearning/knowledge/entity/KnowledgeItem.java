package com.languagelearning.knowledge.entity;

import com.languagelearning.book.entity.Book;
import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.topic.entity.Topic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Common shape for every extracted piece of language knowledge: vocabulary, grammar, and
 * expressions (REQUIREMENTS.md - "Understanding the Book"). Modeled as one table-per-hierarchy so
 * a single scope query (see scope/KnowledgeQueryService) can filter across all three types at
 * once. Deliberately contains no language-specific fields or logic (SPEC.md - architecture goal).
 */
@Entity
@Table(name = "knowledge_item")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class KnowledgeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_node_id")
    private StructureNode structureNode;

    @Column(name = "page")
    private Integer page;

    /** The headword/title of the item: the vocabulary word, grammar point title, or expression phrase. */
    @Column(name = "headword", nullable = false, columnDefinition = "text")
    private String headword;

    /** Meaning / explanation, in the book's explanation language. */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    /** Original excerpt this item was extracted from, kept for provenance/debugging. */
    @Column(name = "source_excerpt", columnDefinition = "text")
    private String sourceExcerpt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "knowledge_item_topic",
            joinColumns = @JoinColumn(name = "knowledge_item_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    @Builder.Default
    private Set<Topic> topics = new HashSet<>();

    public abstract KnowledgeItemType getType();
}
