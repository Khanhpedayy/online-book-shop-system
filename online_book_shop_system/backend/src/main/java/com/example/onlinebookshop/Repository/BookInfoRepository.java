package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {
}
