package com.languagelearning.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("EXPRESSION")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ExpressionItem extends KnowledgeItem {

    @Column(name = "usage_notes", columnDefinition = "text")
    private String usageNotes;

    @Override
    public KnowledgeItemType getType() {
        return KnowledgeItemType.EXPRESSION;
    }
}
