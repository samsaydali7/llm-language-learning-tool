package com.languagelearning.audio.api;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.audio.entity.AudioReference;

public class AudioDtos {

    private AudioDtos() {
    }

    public record AudioFileResponse(Long id, Long bookId, String originalFilename, String contentType) {
        public static AudioFileResponse from(AudioFile audioFile) {
            return new AudioFileResponse(
                    audioFile.getId(), audioFile.getBook().getId(), audioFile.getOriginalFilename(), audioFile.getContentType());
        }
    }

    public record AudioReferenceResponse(
            Long id,
            Long structureNodeId,
            Integer page,
            String label,
            String rawContext,
            Long audioFileId,
            String audioFileName,
            Double matchConfidence) {

        public static AudioReferenceResponse from(AudioReference reference) {
            return new AudioReferenceResponse(
                    reference.getId(),
                    reference.getStructureNode() != null ? reference.getStructureNode().getId() : null,
                    reference.getPage(),
                    reference.getLabel(),
                    reference.getRawContext(),
                    reference.getAudioFile() != null ? reference.getAudioFile().getId() : null,
                    reference.getAudioFile() != null ? reference.getAudioFile().getOriginalFilename() : null,
                    reference.getMatchConfidence());
        }
    }

    public record LinkAudioReferenceRequest(Long audioFileId) {
    }
}
