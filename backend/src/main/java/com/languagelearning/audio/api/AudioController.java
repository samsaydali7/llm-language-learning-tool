package com.languagelearning.audio.api;

import com.languagelearning.audio.AudioIngestionService;
import com.languagelearning.audio.api.AudioDtos.AudioReferenceResponse;
import com.languagelearning.audio.api.AudioDtos.LinkAudioReferenceRequest;
import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.storage.StorageService;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AudioController {

    private final AudioIngestionService audioIngestionService;
    private final StorageService storageService;

    @GetMapping("/audio-files/{id}/stream")
    public ResponseEntity<InputStreamResource> stream(@PathVariable Long id) {
        AudioFile audioFile = audioIngestionService.getAudioFile(id);
        InputStream inputStream = storageService.open(audioFile.getStoragePath());
        long size = storageService.sizeOf(audioFile.getStoragePath());
        MediaType mediaType = audioFile.getContentType() != null
                ? MediaType.parseMediaType(audioFile.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + audioFile.getOriginalFilename() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(size)
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/books/{bookId}/audio-references")
    public java.util.List<AudioReferenceResponse> references(@PathVariable Long bookId) {
        return audioIngestionService.findReferencesByBook(bookId).stream().map(AudioReferenceResponse::from).toList();
    }

    @PutMapping("/audio-references/{id}/link")
    public AudioReferenceResponse link(@PathVariable Long id, @RequestBody LinkAudioReferenceRequest request) {
        return AudioReferenceResponse.from(audioIngestionService.linkManually(id, request.audioFileId()));
    }
}
