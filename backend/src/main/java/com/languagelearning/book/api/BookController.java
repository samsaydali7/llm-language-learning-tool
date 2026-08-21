package com.languagelearning.book.api;

import com.languagelearning.audio.AudioIngestionService;
import com.languagelearning.audio.api.AudioDtos.AudioFileResponse;
import com.languagelearning.book.api.BookDtos.BookResponse;
import com.languagelearning.book.api.BookDtos.CreateBookRequest;
import com.languagelearning.book.entity.Book;
import com.languagelearning.book.service.BookService;
import com.languagelearning.job.api.JobDtos.ExtractionJobResponse;
import com.languagelearning.job.service.ExtractionJobService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ExtractionJobService extractionJobService;
    private final AudioIngestionService audioIngestionService;

    @GetMapping
    public List<BookResponse> list(@RequestParam(required = false) Long languageId) {
        List<Book> books = languageId != null
                ? bookService.findByLanguage(languageId)
                : bookService.findAll();
        return books.stream().map(BookResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return BookResponse.from(bookService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        return BookResponse.from(bookService.create(
                request.languageId(), request.title(), request.description(), request.explanationLanguageCode()));
    }

    @PostMapping("/{id}/pdf")
    public ExtractionJobResponse uploadPdf(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ExtractionJobResponse.from(bookService.uploadPdf(id, file));
    }

    @PostMapping("/{id}/audio")
    public List<AudioFileResponse> uploadAudio(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        return audioIngestionService.uploadAudio(id, files).stream().map(AudioFileResponse::from).toList();
    }

    @GetMapping("/{id}/extraction-jobs")
    public List<ExtractionJobResponse> extractionJobs(@PathVariable Long id) {
        return extractionJobService.findByBook(id).stream().map(ExtractionJobResponse::from).toList();
    }
}
