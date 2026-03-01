package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.BookVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookVariantRepository extends JpaRepository<BookVariant, Long> {

    @Query("SELECT v FROM BookVariant v JOIN FETCH v.book b WHERE v.deletedAt IS NULL AND b.deletedAt IS NULL AND v.isActive = true")
    List<BookVariant> findAllActiveWithBook();
}
