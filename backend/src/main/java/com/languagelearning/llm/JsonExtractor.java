package com.languagelearning.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Local models frequently wrap JSON in markdown fences, add a preamble/postamble, or emit minor
 * formatting noise. This extracts the outermost JSON object/array from raw model output before
 * handing it to Jackson, so parsing stays robust without relying on a specific model's exact
 * formatting habits.
 */
public final class JsonExtractor {

    private JsonExtractor() {
    }

    public static <T> T parse(ObjectMapper mapper, String rawResponse, Class<T> type) {
        String json = extractJson(rawResponse);
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new LlmResponseParseException(
                    "Could not parse model output as " + type.getSimpleName() + ": " + truncate(rawResponse), e);
        }
    }

    static String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new LlmResponseParseException("Model returned an empty response");
        }
        String text = rawResponse.strip();
        text = text.replaceAll("(?s)```json", "```").replaceAll("(?s)```", "");

        int firstObj = text.indexOf('{');
        int firstArr = text.indexOf('[');
        int start;
        char open;
        char close;
        if (firstObj == -1 && firstArr == -1) {
            throw new LlmResponseParseException("Model output did not contain any JSON: " + truncate(rawResponse));
        } else if (firstArr == -1 || (firstObj != -1 && firstObj < firstArr)) {
            start = firstObj;
            open = '{';
            close = '}';
        } else {
            start = firstArr;
            open = '[';
            close = ']';
        }

        int depth = 0;
        int end = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    end = i;
                    break;
                }
            }
        }
        if (end == -1) {
            throw new LlmResponseParseException("Model output had unbalanced JSON: " + truncate(rawResponse));
        }
        return text.substring(start, end + 1);
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}
