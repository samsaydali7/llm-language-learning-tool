package com.languagelearning.topic.repository;

import com.languagelearning.topic.entity.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByLanguageIdAndNameIgnoreCase(Long languageId, String name);

    List<Topic> findByLanguageIdOrderByNameAsc(Long languageId);
}
