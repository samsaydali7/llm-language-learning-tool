package com.languagelearning.common;

import java.util.Locale;
import java.util.Map;

/**
 * Best-effort display name for a language code (used only to phrase prompts to the LLM, e.g.
 * "explanations in English"). Falls back to the JDK's own locale display name, and finally to the
 * raw code, so this never blocks adding a new language - it is metadata, not business logic.
 */
public final class LanguageNames {

    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry("en", "English"),
            Map.entry("fr", "French"),
            Map.entry("es", "Spanish"),
            Map.entry("de", "German"),
            Map.entry("it", "Italian"),
            Map.entry("pt", "Portuguese"),
            Map.entry("hu", "Hungarian"),
            Map.entry("nl", "Dutch"),
            Map.entry("pl", "Polish"),
            Map.entry("ja", "Japanese"),
            Map.entry("zh", "Chinese"),
            Map.entry("ko", "Korean"),
            Map.entry("ru", "Russian"));

    private LanguageNames() {
    }

    public static String displayName(String code) {
        if (code == null || code.isBlank()) {
            return "the target language";
        }
        String normalized = code.strip().toLowerCase(Locale.ROOT);
        if (OVERRIDES.containsKey(normalized)) {
            return OVERRIDES.get(normalized);
        }
        Locale locale = Locale.forLanguageTag(normalized);
        String displayName = locale.getDisplayLanguage(Locale.ENGLISH);
        return (displayName == null || displayName.isBlank() || displayName.equals(normalized)) ? code : displayName;
    }
}
