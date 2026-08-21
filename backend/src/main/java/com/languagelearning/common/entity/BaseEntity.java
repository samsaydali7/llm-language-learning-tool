package com.languagelearning.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Common identity + creation timestamp shared by all persistent entities. Carries {@code @SuperBuilder}
 * (alongside an explicit no-args constructor, which JPA requires and which {@code @SuperBuilder}
 * would otherwise suppress) so that {@link com.languagelearning.knowledge.entity.KnowledgeItem}'s
 * builder hierarchy can chain up to these common fields.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
