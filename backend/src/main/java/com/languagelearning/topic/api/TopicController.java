package com.languagelearning.topic.api;

import com.languagelearning.topic.entity.Topic;
import com.languagelearning.topic.service.TopicService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public List<TopicResponse> list(@RequestParam Long languageId) {
        return topicService.findByLanguage(languageId).stream().map(TopicResponse::from).toList();
    }

    public record TopicResponse(Long id, String name, String description) {
        static TopicResponse from(Topic topic) {
            return new TopicResponse(topic.getId(), topic.getName(), topic.getDescription());
        }
    }
}
