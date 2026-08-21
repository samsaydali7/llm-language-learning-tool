package com.languagelearning.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesPlainJson() {
        Map<String, Object> result = JsonExtractor.parse(mapper, "{\"a\": 1}", Map.class);
        assertThat(result).containsEntry("a", 1);
    }

    @Test
    void stripsMarkdownCodeFences() {
        String raw = "Sure, here you go:\n```json\n{\"a\": 1}\n```\nHope that helps!";
        Map<String, Object> result = JsonExtractor.parse(mapper, raw, Map.class);
        assertThat(result).containsEntry("a", 1);
    }

    @Test
    void ignoresBracesInsideStringValues() {
        String raw = "{\"a\": \"looks like } but isn't\"}";
        Map<String, Object> result = JsonExtractor.parse(mapper, raw, Map.class);
        assertThat(result).containsEntry("a", "looks like } but isn't");
    }

    @Test
    void extractsArrayWhenThatIsTheOutermostStructure() {
        String raw = "here: [1, 2, 3] done";
        assertThat(JsonExtractor.extractJson(raw)).isEqualTo("[1, 2, 3]");
    }

    @Test
    void throwsALlmResponseParseExceptionWhenNoJsonPresent() {
        assertThatThrownBy(() -> JsonExtractor.parse(mapper, "no json here", Map.class))
                .isInstanceOf(LlmResponseParseException.class);
    }
}
