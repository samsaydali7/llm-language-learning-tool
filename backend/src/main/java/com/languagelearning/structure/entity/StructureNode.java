package com.languagelearning.structure.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One node in a book's organizational tree (volume/chapter/section/subsection). Self-referencing
 * so the tree can express arbitrarily deep structure, e.g.
 * Book 2 -> Chapter 4 -> "At the Restaurant" -> "Ordering Food".
 */
@Entity
@Table(name = "structure_node")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructureNode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private StructureNode parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 32)
    private StructureNodeType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "start_page")
    private Integer startPage;

    @Column(name = "end_page")
    private Integer endPage;
}
