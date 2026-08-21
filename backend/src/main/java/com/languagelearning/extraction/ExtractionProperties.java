package com.languagelearning.extraction;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "extraction")
public record ExtractionProperties(Integer maxChunkChars, List<String> audioReferencePatterns) {

    public ExtractionProperties {
        if (maxChunkChars == null || maxChunkChars <= 0) {
            maxChunkChars = 4000;
        }
        if (audioReferencePatterns == null) {
            audioReferencePatterns = List.of();
        }
    }
}
