package com.languagelearning.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("VOCABULARY")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class VocabularyItem extends KnowledgeItem {

    @Column(name = "part_of_speech")
    private String partOfSpeech;

    @Override
    public KnowledgeItemType getType() {
        return KnowledgeItemType.VOCABULARY;
    }
}
