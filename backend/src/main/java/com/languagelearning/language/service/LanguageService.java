package com.languagelearning.language.service;

import com.languagelearning.common.exception.InvalidRequestException;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.language.entity.Language;
import com.languagelearning.language.repository.LanguageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;

    public Language create(String code, String name) {
        if (languageRepository.existsByCodeIgnoreCase(code)) {
            throw new InvalidRequestException("A language with code '" + code + "' already exists");
        }
        Language language = Language.builder().code(code.toLowerCase()).name(name).build();
        return languageRepository.save(language);
    }

    @Transactional(readOnly = true)
    public List<Language> findAll() {
        return languageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Language getById(Long id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Language", id));
    }
}
