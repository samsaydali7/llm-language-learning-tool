package com.languagelearning.book.api;

import com.languagelearning.book.entity.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookDtos {

    private BookDtos() {
    }

    public record CreateBookRequest(
            @NotNull Long languageId,
            @NotBlank String title,
            String description,
            @NotBlank String explanationLanguageCode) {
    }

    public record BookResponse(
            Long id,
            Long languageId,
            String languageName,
            String title,
            String description,
            String explanationLanguageCode,
            boolean hasPdf,
            Integer pdfPageCount) {

        public static BookResponse from(Book book) {
            return new BookResponse(
                    book.getId(),
                    book.getLanguage().getId(),
                    book.getLanguage().getName(),
                    book.getTitle(),
                    book.getDescription(),
                    book.getExplanationLanguageCode(),
                    book.getPdfPath() != null,
                    book.getPdfPageCount());
        }
    }
}
