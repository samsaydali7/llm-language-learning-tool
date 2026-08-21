package com.languagelearning.language.entity;

import com.languagelearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A language the user is learning (e.g. French, Hungarian). The application contains no
 * language-specific logic; a Language is just an identity + display metadata row.
 */
@Entity
@Table(name = "language")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language extends BaseEntity {

    /** ISO-639-1-ish short code, e.g. "fr", "hu". Unique. */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
