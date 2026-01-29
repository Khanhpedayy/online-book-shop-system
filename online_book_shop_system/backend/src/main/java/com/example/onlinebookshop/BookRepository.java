package com.example.onlinebookshop;

import com.example.onlinebookshop.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
