package com.languagelearning.knowledge.api;

import com.languagelearning.knowledge.entity.ExpressionItem;
import com.languagelearning.knowledge.entity.GrammarPoint;
import com.languagelearning.knowledge.entity.KnowledgeExample;
import com.languagelearning.knowledge.entity.KnowledgeItem;
import com.languagelearning.knowledge.entity.VocabularyItem;
import java.util.List;

public class KnowledgeDtos {

    private KnowledgeDtos() {
    }

    public record ExampleResponse(String text, String translation, Integer page) {
        public static ExampleResponse from(KnowledgeExample example) {
            return new ExampleResponse(example.getExampleText(), example.getTranslation(), example.getPage());
        }
    }

    public record KnowledgeItemResponse(
            Long id,
            String type,
            Long bookId,
            Long structureNodeId,
            Integer page,
            String headword,
            String summary,
            String notes,
            String detail,
            List<String> topics,
            List<ExampleResponse> examples) {

        public static KnowledgeItemResponse from(KnowledgeItem item, List<ExampleResponse> examples) {
            String detail = switch (item) {
                case VocabularyItem v -> v.getPartOfSpeech();
                case GrammarPoint g -> g.getPatternText();
                case ExpressionItem e -> e.getUsageNotes();
                default -> null;
            };
            return new KnowledgeItemResponse(
                    item.getId(),
                    item.getType().name(),
                    item.getBook().getId(),
                    item.getStructureNode() != null ? item.getStructureNode().getId() : null,
                    item.getPage(),
                    item.getHeadword(),
                    item.getSummary(),
                    item.getNotes(),
                    detail,
                    item.getTopics().stream().map(t -> t.getName()).toList(),
                    examples);
        }
    }
}
