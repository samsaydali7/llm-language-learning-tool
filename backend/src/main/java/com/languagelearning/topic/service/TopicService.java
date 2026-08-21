package com.languagelearning.topic.service;

import com.languagelearning.language.entity.Language;
import com.languagelearning.topic.entity.Topic;
import com.languagelearning.topic.repository.TopicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;

    /** Finds a topic by name for the language, creating it if extraction has discovered it for the first time. */
    public Topic findOrCreate(Language language, String name) {
        String trimmed = name.strip();
        return topicRepository.findByLanguageIdAndNameIgnoreCase(language.getId(), trimmed)
                .orElseGet(() -> topicRepository.save(Topic.builder().language(language).name(trimmed).build()));
    }

    @Transactional(readOnly = true)
    public List<Topic> findByLanguage(Long languageId) {
        return topicRepository.findByLanguageIdOrderByNameAsc(languageId);
    }
}
