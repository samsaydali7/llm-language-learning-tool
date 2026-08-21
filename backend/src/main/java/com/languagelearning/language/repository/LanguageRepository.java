package com.languagelearning.language.repository;

import com.languagelearning.language.entity.Language;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
