package com.languagelearning.job.listener;

import com.languagelearning.audio.AudioIngestionService;
import com.languagelearning.audio.AudioReferenceMatcher;
import com.languagelearning.audio.entity.AudioReference;
import com.languagelearning.audio.repository.AudioReferenceRepository;
import com.languagelearning.book.entity.Book;
import com.languagelearning.book.service.BookService;
import com.languagelearning.extraction.HeuristicStructureExtractor;
import com.languagelearning.extraction.PdfStructureExtractor;
import com.languagelearning.extraction.TranscriptExtractor;
import com.languagelearning.extraction.model.ExtractedNode;
import com.languagelearning.job.entity.ExtractionJob;
import com.languagelearning.job.messaging.JobPublisher;
import com.languagelearning.job.messaging.KnowledgeExtractionMessage;
import com.languagelearning.job.messaging.RabbitConfig;
import com.languagelearning.job.messaging.StructureExtractionMessage;
import com.languagelearning.job.service.ExtractionJobService;
import com.languagelearning.storage.StorageService;
import com.languagelearning.structure.KnowledgeCoverageRange;
import com.languagelearning.structure.entity.StructureNode;
import com.languagelearning.structure.repository.StructureNodeRepository;
import com.languagelearning.structure.service.StructureNodeService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Handles {@link StructureExtractionMessage}s: builds the book's {@code StructureNode} tree from
 * its PDF (outline first, heuristic fallback), scans every page for audio markers, then fans out
 * one {@link KnowledgeExtractionMessage} per leaf section to start knowledge extraction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StructureExtractionListener {

    private final BookService bookService;
    private final StorageService storageService;
    private final PdfStructureExtractor pdfStructureExtractor;
    private final HeuristicStructureExtractor heuristicStructureExtractor;
    private final TranscriptExtractor transcriptExtractor;
    private final AudioReferenceMatcher audioReferenceMatcher;
    private final AudioReferenceRepository audioReferenceRepository;
    private final AudioIngestionService audioIngestionService;
    private final StructureNodeRepository structureNodeRepository;
    private final StructureNodeService structureNodeService;
    private final ExtractionJobService extractionJobService;
    private final JobPublisher jobPublisher;

    @RabbitListener(queues = RabbitConfig.STRUCTURE_EXTRACTION_QUEUE)
    public void onMessage(StructureExtractionMessage message) {
        Long jobId = message.jobId();
        try {
            extractionJobService.markRunning(jobId);
            Book book = bookService.getById(message.bookId());
            List<KnowledgeExtractionMessage> knowledgeMessages = new ArrayList<>();
            try (PDDocument document = Loader.loadPDF(storageService.open(book.getPdfPath()).readAllBytes())) {
                List<ExtractedNode> tree = pdfStructureExtractor.extract(document);
                if (tree.isEmpty()) {
                    log.info("Book {} has no PDF outline; using heuristic structure detection", book.getId());
                    tree = heuristicStructureExtractor.extract(document);
                }
                List<StructureNode> persisted = persistTree(book, tree, null, 0);
                scanForAudioReferences(book, document, persisted);
                log.info("Structure extraction complete for book {}: {} node(s)", book.getId(), persisted.size());

                for (KnowledgeCoverageRange range
                        : structureNodeService.planKnowledgeExtractionRanges(book.getId(), document.getNumberOfPages())) {
                    knowledgeMessages.add(new KnowledgeExtractionMessage(
                            jobId, book.getId(), range.structureNodeId(), range.startPage(), range.endPage()));
                }
                log.info("Planned {} knowledge-extraction message(s) for book {}", knowledgeMessages.size(), book.getId());
            }
            audioIngestionService.rematchUnlinkedReferences(book.getId());

            extractionJobService.advanceToKnowledgeStage(jobId, knowledgeMessages.size());
            for (KnowledgeExtractionMessage knowledgeMessage : knowledgeMessages) {
                jobPublisher.publishKnowledgeExtraction(knowledgeMessage);
            }
        } catch (Exception e) {
            log.error("Structure extraction failed for job {}", jobId, e);
            extractionJobService.markFailed(jobId, e.getMessage());
        }
    }

    private List<StructureNode> persistTree(Book book, List<ExtractedNode> nodes, StructureNode parent, int startIndex) {
        List<StructureNode> saved = new ArrayList<>();
        int index = startIndex;
        for (ExtractedNode node : nodes) {
            StructureNode entity = structureNodeRepository.save(StructureNode.builder()
                    .book(book)
                    .parent(parent)
                    .type(node.getType())
                    .title(node.getTitle())
                    .orderIndex(index++)
                    .startPage(node.getStartPage())
                    .endPage(node.getEndPage())
                    .build());
            saved.add(entity);
            saved.addAll(persistTree(book, node.getChildren(), entity, 0));
        }
        return saved;
    }

    private void scanForAudioReferences(Book book, PDDocument document, List<StructureNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }
        int totalPages = document.getNumberOfPages();
        for (int page = 1; page <= totalPages; page++) {
            String pageText;
            try {
                pageText = transcriptExtractor.extractPage(document, page);
            } catch (Exception e) {
                log.warn("Could not extract text for page {} of book {}: {}", page, book.getId(), e.getMessage());
                continue;
            }
            StructureNode owningNode = findNodeForPage(nodes, page);
            for (AudioReferenceMatcher.DetectedReference ref : audioReferenceMatcher.findReferences(pageText, page)) {
                audioReferenceRepository.save(AudioReference.builder()
                        .book(book)
                        .structureNode(owningNode)
                        .page(page)
                        .label(ref.label())
                        .rawContext(ref.context())
                        .build());
            }
        }
    }

    private StructureNode findNodeForPage(List<StructureNode> nodes, int page) {
        StructureNode best = null;
        for (StructureNode node : nodes) {
            if (node.getStartPage() != null && node.getStartPage() <= page
                    && (node.getEndPage() == null || page <= node.getEndPage())) {
                if (best == null || node.getStartPage() > best.getStartPage()) {
                    best = node;
                }
            }
        }
        return best;
    }
}
