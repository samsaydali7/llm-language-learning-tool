package com.languagelearning.audio;

import com.languagelearning.audio.entity.AudioFile;
import com.languagelearning.extraction.ExtractionProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Finds in-text audio markers (e.g. "Track 12", "CD1-05") and matches them to uploaded
 * {@link AudioFile}s by comparing the digit sequences in the marker against the digit sequences
 * in each file's name (REQUIREMENTS.md - "connect audio files ... whenever possible"). Matching
 * is necessarily best-effort: a reference is only auto-linked when every digit group in the
 * marker is also present in a candidate filename; anything less confident is left for the user to
 * link manually via the book's audio-reference endpoints.
 */
@Component
@RequiredArgsConstructor
public class AudioReferenceMatcher {

    private static final Pattern DIGIT_GROUP = Pattern.compile("\\d+");
    private static final double AUTO_LINK_THRESHOLD = 0.999;

    private final ExtractionProperties extractionProperties;

    public List<DetectedReference> findReferences(String text, int page) {
        List<DetectedReference> found = new ArrayList<>();
        for (String patternStr : extractionProperties.audioReferencePatterns()) {
            Matcher matcher = Pattern.compile(patternStr).matcher(text);
            while (matcher.find()) {
                String label = matcher.group().strip();
                found.add(new DetectedReference(label, contextAround(text, matcher.start(), matcher.end()), page));
            }
        }
        return found;
    }

    /** Best matching audio file for a marker label, or null if no candidate is confident enough to auto-link. */
    public MatchResult matchBest(String label, List<AudioFile> candidates) {
        List<Integer> labelDigits = digitGroups(label);
        if (labelDigits.isEmpty() || candidates.isEmpty()) {
            return new MatchResult(null, 0.0);
        }
        AudioFile best = null;
        double bestScore = 0.0;
        for (AudioFile candidate : candidates) {
            List<Integer> fileDigits = digitGroups(candidate.getOriginalFilename());
            long matching = labelDigits.stream().filter(fileDigits::contains).count();
            double score = (double) matching / labelDigits.size();
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return bestScore >= AUTO_LINK_THRESHOLD ? new MatchResult(best, bestScore) : new MatchResult(null, bestScore);
    }

    private List<Integer> digitGroups(String text) {
        List<Integer> groups = new ArrayList<>();
        Matcher matcher = DIGIT_GROUP.matcher(text);
        while (matcher.find()) {
            groups.add(Integer.parseInt(matcher.group()));
        }
        return groups;
    }

    private String contextAround(String text, int start, int end) {
        int from = Math.max(0, start - 60);
        int to = Math.min(text.length(), end + 60);
        return text.substring(from, to).strip();
    }

    public record DetectedReference(String label, String context, int page) {
    }

    public record MatchResult(AudioFile audioFile, double confidence) {
    }
}
