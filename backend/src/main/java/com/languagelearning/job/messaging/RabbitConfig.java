package com.languagelearning.job.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the async job topology (SPEC.md #2 - "persistent, job-based exercise generation" and
 * async knowledge extraction). One topic exchange with a queue per workload, each backed by a
 * dead-letter queue so a poison message (e.g. a malformed LLM response after retries are
 * exhausted) doesn't block the pipeline.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "learning.exchange";
    public static final String DLX = "learning.exchange.dlx";

    public static final String STRUCTURE_EXTRACTION_QUEUE = "structure.extraction.queue";
    public static final String KNOWLEDGE_EXTRACTION_QUEUE = "knowledge.extraction.queue";
    public static final String EXERCISE_GENERATION_QUEUE = "exercise.generation.queue";

    public static final String ROUTING_STRUCTURE_EXTRACTION = "structure.extraction";
    public static final String ROUTING_KNOWLEDGE_EXTRACTION = "knowledge.extraction";
    public static final String ROUTING_EXERCISE_GENERATION = "exercise.generation";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange learningExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    @Bean
    public Queue structureExtractionQueue() {
        return workloadQueue(STRUCTURE_EXTRACTION_QUEUE, ROUTING_STRUCTURE_EXTRACTION);
    }

    @Bean
    public Queue knowledgeExtractionQueue() {
        return workloadQueue(KNOWLEDGE_EXTRACTION_QUEUE, ROUTING_KNOWLEDGE_EXTRACTION);
    }

    @Bean
    public Queue exerciseGenerationQueue() {
        return workloadQueue(EXERCISE_GENERATION_QUEUE, ROUTING_EXERCISE_GENERATION);
    }

    @Bean
    public Queue structureExtractionDlq() {
        return QueueBuilder.durable(STRUCTURE_EXTRACTION_QUEUE + ".dlq").build();
    }

    @Bean
    public Queue knowledgeExtractionDlq() {
        return QueueBuilder.durable(KNOWLEDGE_EXTRACTION_QUEUE + ".dlq").build();
    }

    @Bean
    public Queue exerciseGenerationDlq() {
        return QueueBuilder.durable(EXERCISE_GENERATION_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding structureExtractionBinding() {
        return BindingBuilder.bind(structureExtractionQueue()).to(learningExchange()).with(ROUTING_STRUCTURE_EXTRACTION);
    }

    @Bean
    public Binding knowledgeExtractionBinding() {
        return BindingBuilder.bind(knowledgeExtractionQueue()).to(learningExchange()).with(ROUTING_KNOWLEDGE_EXTRACTION);
    }

    @Bean
    public Binding exerciseGenerationBinding() {
        return BindingBuilder.bind(exerciseGenerationQueue()).to(learningExchange()).with(ROUTING_EXERCISE_GENERATION);
    }

    @Bean
    public Binding structureExtractionDlqBinding() {
        return BindingBuilder.bind(structureExtractionDlq()).to(deadLetterExchange()).with(ROUTING_STRUCTURE_EXTRACTION);
    }

    @Bean
    public Binding knowledgeExtractionDlqBinding() {
        return BindingBuilder.bind(knowledgeExtractionDlq()).to(deadLetterExchange()).with(ROUTING_KNOWLEDGE_EXTRACTION);
    }

    @Bean
    public Binding exerciseGenerationDlqBinding() {
        return BindingBuilder.bind(exerciseGenerationDlq()).to(deadLetterExchange()).with(ROUTING_EXERCISE_GENERATION);
    }

    private Queue workloadQueue(String name, String routingKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .build();
    }
}
