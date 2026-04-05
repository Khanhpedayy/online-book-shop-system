package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE BookInfo b
            SET b.stockQuantity = coalesce(b.stockQuantity, 0) - :qty
            WHERE b.id = :bookId
              AND b.deletedAt IS NULL
              AND coalesce(b.stockQuantity, 0) >= :qty
            """)
    int decrementStockBy(@Param("bookId") Long bookId, @Param("qty") int qty);
}
