package com.languagelearning.book.repository;

import com.languagelearning.book.entity.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByLanguageId(Long languageId);
}
