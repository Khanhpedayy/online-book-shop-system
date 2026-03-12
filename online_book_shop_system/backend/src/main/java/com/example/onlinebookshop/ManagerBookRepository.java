package com.example.onlinebookshop;

import com.example.onlinebookshop.ManagerBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerBookRepository extends JpaRepository<ManagerBook, Long> {
}

