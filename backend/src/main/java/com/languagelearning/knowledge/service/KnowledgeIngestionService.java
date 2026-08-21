package com.languagelearning.knowledge.service;

import com.languagelearning.book.entity.Book;
import com.languagelearning.knowledge.entity.ExpressionItem;
import com.languagelearning.knowledge.entity.GrammarPoint;
import com.languagelearning.knowledge.entity.KnowledgeExample;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.entity.VocabularyItem;
import com.languagelearning.knowledge.repository.KnowledgeExampleRepository;
import com.languagelearning.knowledge.repository.KnowledgeItemRepository;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import com.languagelearning.llm.dto.KnowledgeExtractionResult.ExampleExtract;
import com.languagelearning.llm.dto.KnowledgeExtractionResult.ExpressionExtract;
import com.languagelearning.llm.dto.KnowledgeExtractionResult.GrammarExtract;
import com.languagelearning.llm.dto.KnowledgeExtractionResult.VocabularyExtract;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.topic.entity.Topic;
import com.languagelearning.topic.service.TopicService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Turns one chunk's {@link KnowledgeExtractionResult} into persisted knowledge base rows. */
@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeIngestionService {

    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeExampleRepository knowledgeExampleRepository;
    private final TopicService topicService;

    public int persist(Book book, StructureNode node, Integer page, String sourceExcerpt, KnowledgeExtractionResult result) {
        Set<Topic> topics = new HashSet<>();
        for (String topicName : result.topics()) {
            if (topicName != null && !topicName.isBlank()) {
                topics.add(topicService.findOrCreate(book.getLanguage(), topicName));
            }
        }

        int count = 0;
        for (VocabularyExtract v : result.vocabulary()) {
            if (isBlank(v.headword())) {
                continue;
            }
            VocabularyItem item = VocabularyItem.builder()
                    .book(book).structureNode(node).page(page)
                    .headword(v.headword()).summary(v.meaning()).notes(v.notes())
                    .sourceExcerpt(sourceExcerpt).partOfSpeech(v.partOfSpeech())
                    .topics(topics)
                    .build();
            saveWithExamples(item, v.examples(), page);
            count++;
        }
        for (GrammarExtract g : result.grammar()) {
            if (isBlank(g.title())) {
                continue;
            }
            GrammarPoint item = GrammarPoint.builder()
                    .book(book).structureNode(node).page(page)
                    .headword(g.title()).summary(g.explanation())
                    .sourceExcerpt(sourceExcerpt).patternText(g.patterns())
                    .topics(topics)
                    .build();
            saveWithExamples(item, g.examples(), page);
            count++;
        }
        for (ExpressionExtract e : result.expressions()) {
            if (isBlank(e.phrase())) {
                continue;
            }
            ExpressionItem item = ExpressionItem.builder()
                    .book(book).structureNode(node).page(page)
                    .headword(e.phrase()).summary(e.meaning())
                    .sourceExcerpt(sourceExcerpt).usageNotes(e.usageNotes())
                    .topics(topics)
                    .build();
            saveWithExamples(item, e.examples(), page);
            count++;
        }
        return count;
    }

    private void saveWithExamples(KnowledgeItem item, List<ExampleExtract> examples, Integer page) {
        KnowledgeItem saved = knowledgeItemRepository.save(item);
        if (examples == null) {
            return;
        }
        for (ExampleExtract example : examples) {
            if (isBlank(example.text())) {
                continue;
            }
            knowledgeExampleRepository.save(KnowledgeExample.builder()
                    .knowledgeItem(saved)
                    .exampleText(example.text())
                    .translation(example.translation())
                    .page(page)
                    .build());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
