package com.languagelearning.language.api;

import com.languagelearning.language.api.LanguageDtos.CreateLanguageRequest;
import com.languagelearning.language.api.LanguageDtos.LanguageResponse;
import com.languagelearning.language.service.LanguageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public List<LanguageResponse> list() {
        return languageService.findAll().stream().map(LanguageResponse::from).toList();
    }

    @GetMapping("/{id}")
    public LanguageResponse get(@PathVariable Long id) {
        return LanguageResponse.from(languageService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LanguageResponse create(@Valid @RequestBody CreateLanguageRequest request) {
        return LanguageResponse.from(languageService.create(request.code(), request.name()));
    }
}
