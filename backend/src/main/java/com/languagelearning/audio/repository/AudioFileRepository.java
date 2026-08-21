package com.languagelearning.audio.repository;

import com.languagelearning.audio.entity.AudioFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {

    List<AudioFile> findByBookId(Long bookId);
}
