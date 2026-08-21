package com.languagelearning.job.listener;

import com.languagelearning.book.entity.Book;
import com.languagelearning.book.service.BookService;
import com.languagelearning.common.LanguageNames;
import com.languagelearning.extraction.ExtractionProperties;
import com.languagelearning.extraction.TextChunker;
import com.languagelearning.extraction.TranscriptExtractor;
import com.languagelearning.job.messaging.KnowledgeExtractionMessage;
import com.languagelearning.job.messaging.RabbitConfig;
import com.languagelearning.job.service.ExtractionJobService;
import com.languagelearning.knowledge.service.KnowledgeIngestionService;
import com.languagelearning.llm.LlmRouter;
import com.languagelearning.llm.dto.KnowledgeExtractionRequest;
import com.languagelearning.llm.dto.KnowledgeExtractionResult;
import com.languagelearning.storage.StorageService;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.structure.service.StructureNodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Handles {@link KnowledgeExtractionMessage}s: extracts one leaf section's text, chunks it to fit
 * the extraction model's context window, calls {@link LlmRouter} for each chunk, and persists the
 * resulting vocabulary/grammar/expressions/examples/topics. A chunk failure is logged and skipped
 * rather than failing the whole book, so one bad section doesn't stall the rest of the ingestion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeExtractionListener {

    private final BookService bookService;
    private final StorageService storageService;
    private final StructureNodeService structureNodeService;
    private final TranscriptExtractor transcriptExtractor;
    private final ExtractionProperties extractionProperties;
    private final LlmRouter llmRouter;
    private final KnowledgeIngestionService knowledgeIngestionService;
    private final ExtractionJobService extractionJobService;

    @RabbitListener(queues = RabbitConfig.KNOWLEDGE_EXTRACTION_QUEUE)
    public void onMessage(KnowledgeExtractionMessage message) {
        Book book = bookService.getById(message.bookId());
        StructureNode node = structureNodeService.getById(message.structureNodeId());
        int startPage = message.startPage() != null ? message.startPage()
                : node.getStartPage() != null ? node.getStartPage() : 1;
        int endPage = message.endPage() != null ? message.endPage()
                : node.getEndPage() != null ? node.getEndPage() : startPage;
        int itemsCreated = 0;
        try {
            String text = extractSectionText(book, startPage, endPage);
            List<String> chunks = TextChunker.chunk(text, extractionProperties.maxChunkChars());
            for (String chunk : chunks) {
                try {
                    KnowledgeExtractionResult result = llmRouter.extractKnowledge(new KnowledgeExtractionRequest(
                            book.getLanguage().getName(),
                            LanguageNames.displayName(book.getExplanationLanguageCode()),
                            book.getTitle(),
                            node.getTitle(),
                            chunk,
                            startPage,
                            null));
                    itemsCreated += knowledgeIngestionService.persist(book, node, startPage, chunk, result);
                } catch (Exception e) {
                    log.error("Knowledge extraction failed for a chunk of section '{}' pages {}-{} (book {}): {}",
                            node.getTitle(), startPage, endPage, book.getId(), e.getMessage());
                }
            }
            log.info("Extracted {} knowledge item(s) from section '{}' pages {}-{} (book {})",
                    itemsCreated, node.getTitle(), startPage, endPage, book.getId());
        } catch (Exception e) {
            log.error("Could not process section '{}' pages {}-{} for book {}", node.getTitle(), startPage, endPage, book.getId(), e);
        } finally {
            extractionJobService.recordSectionCompleted(message.jobId());
        }
    }

    private String extractSectionText(Book book, int startPage, int endPage) throws Exception {
        try (PDDocument document = Loader.loadPDF(storageService.open(book.getPdfPath()).readAllBytes())) {
            return transcriptExtractor.extractPages(document, startPage, endPage);
        }
    }
}
