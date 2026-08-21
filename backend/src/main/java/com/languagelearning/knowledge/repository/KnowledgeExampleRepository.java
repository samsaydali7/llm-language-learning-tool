package com.languagelearning.knowledge.repository;

import com.languagelearning.knowledge.entity.KnowledgeExample;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeExampleRepository extends JpaRepository<KnowledgeExample, Long> {

    List<KnowledgeExample> findByKnowledgeItemId(Long knowledgeItemId);

    List<KnowledgeExample> findByKnowledgeItemIdIn(List<Long> knowledgeItemIds);
}
