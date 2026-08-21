package com.languagelearning.knowledge.entity;

import com.languagelearning.common.entity.BaseEntity;
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
 * An example sentence preserved for a {@link KnowledgeItem} (REQUIREMENTS.md - "The application
 * should preserve useful examples from the book").
 */
@Entity
@Table(name = "knowledge_example")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeExample extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_item_id", nullable = false)
    private KnowledgeItem knowledgeItem;

    @Column(name = "example_text", nullable = false, columnDefinition = "text")
    private String exampleText;

    @Column(name = "translation", columnDefinition = "text")
    private String translation;

    @Column(name = "page")
    private Integer page;
}
