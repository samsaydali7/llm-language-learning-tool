package com.languagelearning.job.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStructureExtraction(StructureExtractionMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_STRUCTURE_EXTRACTION, message);
    }

    public void publishKnowledgeExtraction(KnowledgeExtractionMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KNOWLEDGE_EXTRACTION, message);
    }

    public void publishExerciseGeneration(ExerciseGenerationMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_EXERCISE_GENERATION, message);
    }
}
