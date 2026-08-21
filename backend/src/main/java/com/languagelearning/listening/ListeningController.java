package com.languagelearning.listening;

import com.languagelearning.audio.AudioIngestionService;
import com.languagelearning.audio.api.AudioDtos.AudioFileResponse;
import com.languagelearning.exercise.api.ExerciseDtos.ExerciseResponse;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listening")
@RequiredArgsConstructor
public class ListeningController {

    private final ListeningService listeningService;
    private final AudioIngestionService audioIngestionService;

    @GetMapping("/books/{bookId}/tracks")
    public List<AudioFileResponse> tracks(@PathVariable Long bookId) {
        return listeningService.listByBook(bookId).stream().map(AudioFileResponse::from).toList();
    }

    @GetMapping("/audio-files/{id}/transcript")
    public TranscriptResponse transcript(@PathVariable Long id) {
        return new TranscriptResponse(listeningService.transcriptFor(audioIngestionService.getAudioFile(id)));
    }

    @PostMapping("/audio-files/{id}/exercises")
    public List<ExerciseResponse> exercises(@PathVariable Long id, @RequestParam(defaultValue = "5") @Min(1) int count) {
        return listeningService.generateExercises(id, count).stream().map(ExerciseResponse::from).toList();
    }

    public record TranscriptResponse(String transcript) {
    }
}
