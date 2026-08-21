package com.languagelearning.knowledge.repository;

import com.languagelearning.knowledge.entity.KnowledgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeItemRepository
        extends JpaRepository<KnowledgeItem, Long>, JpaSpecificationExecutor<KnowledgeItem> {

    @Modifying
    @Query("delete from KnowledgeItem k where k.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
