package com.languagelearning.language.api;

import com.languagelearning.language.entity.Language;
import jakarta.validation.constraints.NotBlank;

public class LanguageDtos {

    private LanguageDtos() {
    }

    public record CreateLanguageRequest(
            @NotBlank String code,
            @NotBlank String name) {
    }

    public record LanguageResponse(Long id, String code, String name) {
        public static LanguageResponse from(Language language) {
            return new LanguageResponse(language.getId(), language.getCode(), language.getName());
        }
    }
}
