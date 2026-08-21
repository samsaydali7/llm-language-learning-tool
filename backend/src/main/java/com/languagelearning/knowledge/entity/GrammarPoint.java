package com.languagelearning.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("GRAMMAR")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GrammarPoint extends KnowledgeItem {

    @Column(name = "pattern_text", columnDefinition = "text")
    private String patternText;

    @Override
    public KnowledgeItemType getType() {
        return KnowledgeItemType.GRAMMAR;
    }
}
