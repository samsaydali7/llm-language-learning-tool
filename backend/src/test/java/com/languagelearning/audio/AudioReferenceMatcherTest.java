package com.languagelearning.audio;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.extraction.ExtractionProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioReferenceMatcherTest {

    private AudioReferenceMatcher matcher;

    @BeforeEach
    void setUp() {
        ExtractionProperties properties = new ExtractionProperties(4000,
                List.of("(?i)track\\s?#?\\d+([-.]\\d+)?", "(?i)cd\\s?\\d+[-.]\\d+"));
        matcher = new AudioReferenceMatcher(properties);
    }

    @Test
    void findsTrackReferencesInText() {
        String text = "Listen to Track 12 and repeat the phrases you hear.";
        var refs = matcher.findReferences(text, 42);
        assertThat(refs).hasSize(1);
        assertThat(refs.get(0).label()).isEqualToIgnoringCase("Track 12");
        assertThat(refs.get(0).page()).isEqualTo(42);
    }

    @Test
    void autoLinksWhenAllDigitGroupsMatchAFilename() {
        AudioFile file = AudioFile.builder().originalFilename("track-12.mp3").build();
        AudioReferenceMatcher.MatchResult result = matcher.matchBest("Track 12", List.of(file));
        assertThat(result.audioFile()).isEqualTo(file);
        assertThat(result.confidence()).isEqualTo(1.0);
    }

    @Test
    void doesNotAutoLinkWhenNoFilenameMatches() {
        AudioFile file = AudioFile.builder().originalFilename("intro.mp3").build();
        AudioReferenceMatcher.MatchResult result = matcher.matchBest("Track 12", List.of(file));
        assertThat(result.audioFile()).isNull();
    }

    @Test
    void picksTheBestScoringCandidateAmongSeveral() {
        AudioFile wrong = AudioFile.builder().originalFilename("track-05.mp3").build();
        AudioFile right = AudioFile.builder().originalFilename("track-12.mp3").build();
        AudioReferenceMatcher.MatchResult result = matcher.matchBest("Track 12", List.of(wrong, right));
        assertThat(result.audioFile()).isEqualTo(right);
    }
}
