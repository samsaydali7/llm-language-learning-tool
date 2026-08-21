package com.languagelearning.audio;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.audio.entity.AudioReference;
import com.languagelearning.audio.repository.AudioFileRepository;
import com.languagelearning.audio.repository.AudioReferenceRepository;
import com.languagelearning.book.entity.Book;
import com.languagelearning.book.service.BookService;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.storage.StorageService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AudioIngestionService {

    private final BookService bookService;
    private final StorageService storageService;
    private final AudioFileRepository audioFileRepository;
    private final AudioReferenceRepository audioReferenceRepository;
    private final AudioReferenceMatcher audioReferenceMatcher;

    public List<AudioFile> uploadAudio(Long bookId, List<MultipartFile> files) {
        Book book = bookService.getById(bookId);
        List<AudioFile> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            String relativePath = storageService.store(book.getLanguage().getCode(), book.getId(), "audio", file);
            AudioFile audioFile = audioFileRepository.save(AudioFile.builder()
                    .book(book)
                    .originalFilename(file.getOriginalFilename())
                    .storagePath(relativePath)
                    .contentType(file.getContentType())
                    .build());
            saved.add(audioFile);
        }
        rematchUnlinkedReferences(bookId);
        return saved;
    }

    /** Re-runs matching for any audio reference not yet linked - called after structure extraction and after new audio uploads. */
    public void rematchUnlinkedReferences(Long bookId) {
        List<AudioReference> unmatched = audioReferenceRepository.findByBookIdAndAudioFileIsNull(bookId);
        if (unmatched.isEmpty()) {
            return;
        }
        List<AudioFile> candidates = audioFileRepository.findByBookId(bookId);
        for (AudioReference reference : unmatched) {
            AudioReferenceMatcher.MatchResult result = audioReferenceMatcher.matchBest(reference.getLabel(), candidates);
            if (result.audioFile() != null) {
                reference.setAudioFile(result.audioFile());
                reference.setMatchConfidence(result.confidence());
                audioReferenceRepository.save(reference);
                log.info("Auto-linked audio reference '{}' (page {}) to file {}",
                        reference.getLabel(), reference.getPage(), result.audioFile().getOriginalFilename());
            }
        }
    }

    public AudioReference linkManually(Long referenceId, Long audioFileId) {
        AudioReference reference = audioReferenceRepository.findById(referenceId)
                .orElseThrow(() -> ResourceNotFoundException.of("AudioReference", referenceId));
        AudioFile audioFile = audioFileRepository.findById(audioFileId)
                .orElseThrow(() -> ResourceNotFoundException.of("AudioFile", audioFileId));
        reference.setAudioFile(audioFile);
        reference.setMatchConfidence(1.0);
        return audioReferenceRepository.save(reference);
    }

    @Transactional(readOnly = true)
    public List<AudioFile> findByBook(Long bookId) {
        return audioFileRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<AudioReference> findReferencesByBook(Long bookId) {
        return audioReferenceRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public AudioFile getAudioFile(Long id) {
        return audioFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AudioFile", id));
    }
}
