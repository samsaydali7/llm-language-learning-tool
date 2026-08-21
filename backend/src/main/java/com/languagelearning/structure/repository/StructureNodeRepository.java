package com.languagelearning.structure.repository;

import com.languagelearning.structure.entity.StructureNode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StructureNodeRepository extends JpaRepository<StructureNode, Long> {

    List<StructureNode> findByBookIdOrderByOrderIndexAsc(Long bookId);

    List<StructureNode> findByParentIdOrderByOrderIndexAsc(Long parentId);

    List<StructureNode> findByBookIdAndParentIsNullOrderByOrderIndexAsc(Long bookId);

    /** Nodes with no children: the leaves that knowledge extraction runs against. */
    @Query("select s from StructureNode s where s.book.id = :bookId "
            + "and not exists (select 1 from StructureNode c where c.parent = s) "
            + "order by s.orderIndex asc")
    List<StructureNode> findLeafNodes(@Param("bookId") Long bookId);

    /**
     * Bulk delete (a single SQL statement, not per-entity removal): safe with the self-referential
     * parent/child FK, which relies on the DB's own ON DELETE CASCADE rather than JPA cascading.
     */
    @Modifying
    @Query("delete from StructureNode s where s.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
