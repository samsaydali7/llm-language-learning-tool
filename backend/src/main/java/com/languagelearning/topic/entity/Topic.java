package com.languagelearning.topic.entity;

import com.languagelearning.common.entity.BaseEntity;
import com.languagelearning.language.entity.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A topic such as "Restaurant" or "Travel", scoped to a language. Topics are derived from
 * extracted content rather than drawn from a fixed list (REQUIREMENTS.md - Topics), so they are
 * upserted by name as knowledge extraction discovers them.
 */
@Entity
@Table(name = "topic", uniqueConstraints = @UniqueConstraint(columnNames = {"language_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
