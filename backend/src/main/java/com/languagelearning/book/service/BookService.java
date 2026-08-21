package com.languagelearning.book.service;

import com.languagelearning.audio.repository.AudioReferenceRepository;
import com.languagelearning.book.entity.Book;
import com.languagelearning.book.repository.BookRepository;
import com.languagelearning.common.exception.InvalidRequestException;
import com.languagelearning.common.exception.ResourceNotFoundException;
import com.languagelearning.job.entity.ExtractionJob;
import com.languagelearning.job.service.ExtractionJobService;
import com.languagelearning.knowledge.repository.KnowledgeItemRepository;
import com.languagelearning.language.entity.Language;
import com.languagelearning.language.service.LanguageService;
import com.languagelearning.storage.StorageService;
import com.languagelearning.structure.repository.StructureNodeRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final LanguageService languageService;
    private final StorageService storageService;
    private final ExtractionJobService extractionJobService;
    private final StructureNodeRepository structureNodeRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final AudioReferenceRepository audioReferenceRepository;

    public Book create(Long languageId, String title, String description, String explanationLanguageCode) {
        Language language = languageService.getById(languageId);
        Book book = Book.builder()
                .language(language)
                .title(title)
                .description(description)
                .explanationLanguageCode(explanationLanguageCode.toLowerCase())
                .build();
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> findByLanguage(Long languageId) {
        return bookRepository.findByLanguageId(languageId);
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book getById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Book", id));
    }

    /**
     * Stores the PDF, records its page count, and kicks off async structure + knowledge
     * extraction. Re-uploading a PDF to a book that already has one first clears the previous
     * structure/knowledge/audio-reference data - otherwise a re-upload (e.g. after fixing a bad
     * PDF, or retrying following an extraction bug) would just pile new results on top of the
     * old ones instead of replacing them.
     */
    public ExtractionJob uploadPdf(Long bookId, MultipartFile file) {
        Book book = getById(bookId);
        if (file == null || !"application/pdf".equalsIgnoreCase(file.getContentType()) && !hasPdfExtension(file)) {
            throw new InvalidRequestException("Uploaded file must be a PDF");
        }
        if (book.getPdfPath() != null) {
            log.info("Book {} already had a PDF - clearing its previous structure/knowledge/audio-reference data before re-extracting", bookId);
            audioReferenceRepository.deleteByBookId(bookId);
            knowledgeItemRepository.deleteByBookId(bookId);
            structureNodeRepository.deleteByBookId(bookId);
        }
        String relativePath = storageService.store(book.getLanguage().getCode(), book.getId(), "pdf", file);
        int pageCount = readPageCount(storageService.open(relativePath));
        book.setPdfPath(relativePath);
        book.setPdfPageCount(pageCount);
        bookRepository.save(book);
        log.info("Stored PDF for book {} ({} pages) at {}", bookId, pageCount, relativePath);
        return extractionJobService.startForBook(book);
    }

    private boolean hasPdfExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".pdf");
    }

    private int readPageCount(InputStream inputStream) {
        try (InputStream in = inputStream; PDDocument document = Loader.loadPDF(in.readAllBytes())) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read uploaded PDF", e);
        }
    }
}
