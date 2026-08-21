package com.languagelearning.listening;

import com.languagelearning.audio.AudioIngestionService;
import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.audio.entity.AudioReference;
import com.languagelearning.audio.repository.AudioReferenceRepository;
import com.languagelearning.book.entity.Book;
import com.languagelearning.exercise.ExerciseGenerationParams;
import com.languagelearning.exercise.ExerciseGenerationService;
import com.languagelearning.exercise.entity.Exercise;
import com.languagelearning.exercise.entity.ExerciseType;
import com.languagelearning.extraction.TranscriptExtractor;
import com.languagelearning.scope.StudyScope;
import com.languagelearning.storage.StorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple listening exercises (REQUIREMENTS.md - "Practice listening"; SPEC.md non-goals exclude
 * speech recognition/pronunciation scoring, so this reuses the book's own transcript rather than
 * doing any audio analysis). Reuses {@link ExerciseGenerationService} with the transcript as
 * context, keeping listening on the same shared generation pipeline as every other exercise type.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListeningService {

    private final AudioIngestionService audioIngestionService;
    private final AudioReferenceRepository audioReferenceRepository;
    private final StorageService storageService;
    private final TranscriptExtractor transcriptExtractor;
    private final ExerciseGenerationService exerciseGenerationService;

    public List<AudioFile> listByBook(Long bookId) {
        return audioIngestionService.findByBook(bookId);
    }

    public String transcriptFor(AudioFile audioFile) {
        List<AudioReference> references = audioReferenceRepository.findByAudioFileId(audioFile.getId());
        if (references.isEmpty()) {
            return null;
        }
        AudioReference reference = references.get(0);
        Book book = audioFile.getBook();
        Integer startPage = reference.getStructureNode() != null ? reference.getStructureNode().getStartPage() : reference.getPage();
        Integer endPage = reference.getStructureNode() != null ? reference.getStructureNode().getEndPage() : reference.getPage();
        if (startPage == null) {
            return reference.getRawContext();
        }
        try (PDDocument document = Loader.loadPDF(storageService.open(book.getPdfPath()).readAllBytes())) {
            return transcriptExtractor.extractPages(document, startPage, endPage != null ? endPage : startPage);
        } catch (Exception e) {
            return reference.getRawContext();
        }
    }

    @Transactional
    public List<Exercise> generateExercises(Long audioFileId, int count) {
        AudioFile audioFile = audioIngestionService.getAudioFile(audioFileId);
        String transcript = transcriptFor(audioFile);
        List<AudioReference> references = audioReferenceRepository.findByAudioFileId(audioFileId);
        List<Long> structureNodeIds = references.isEmpty() || references.get(0).getStructureNode() == null
                ? List.of() : List.of(references.get(0).getStructureNode().getId());

        StudyScope scope = new StudyScope(null, audioFile.getBook().getId(), structureNodeIds, List.of(), List.of());
        return exerciseGenerationService.generate(new ExerciseGenerationParams(
                scope, count, List.of(ExerciseType.LISTENING_COMPREHENSION), true, null, transcript, audioFile));
    }
}
