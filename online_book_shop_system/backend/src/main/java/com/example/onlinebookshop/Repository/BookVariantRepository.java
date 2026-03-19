package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.BookVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookVariantRepository extends JpaRepository<BookVariant, Long> {

    @Query("SELECT v FROM BookVariant v JOIN FETCH v.book b WHERE v.deletedAt IS NULL AND b.deletedAt IS NULL AND v.isActive = true")
    List<BookVariant> findAllActiveWithBook();

    List<BookVariant> findByBookId(Long bookId);

    @Query("SELECT v FROM BookVariant v JOIN v.book b " +
            "WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:publisherName IS NULL OR b.publisherName = :publisherName) " +
            "AND (:minPrice IS NULL OR v.salePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.salePrice <= :maxPrice) " +
            "AND v.isActive = true AND v.deletedAt IS NULL AND b.deletedAt IS NULL")
    List<BookVariant> findBooksFiltered(
            @Param("keyword") String keyword,
            @Param("publisherName") String publisherName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}
