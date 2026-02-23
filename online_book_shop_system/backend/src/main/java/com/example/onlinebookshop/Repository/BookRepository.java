package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
