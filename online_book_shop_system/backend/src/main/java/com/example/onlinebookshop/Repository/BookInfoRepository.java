package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {

    List<BookInfo> findByCategoryId(Long categoryId);
}
