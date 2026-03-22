package com.example.onlinebookshop.repository;

import com.example.onlinebookshop.entity.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {
}
