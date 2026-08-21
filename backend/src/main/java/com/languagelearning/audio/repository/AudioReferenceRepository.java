package com.languagelearning.audio.repository;

import com.languagelearning.audio.entity.AudioReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AudioReferenceRepository extends JpaRepository<AudioReference, Long> {

    List<AudioReference> findByBookId(Long bookId);

    List<AudioReference> findByBookIdAndAudioFileIsNull(Long bookId);

    List<AudioReference> findByStructureNodeId(Long structureNodeId);

    List<AudioReference> findByAudioFileId(Long audioFileId);

    @Modifying
    @Query("delete from AudioReference a where a.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
