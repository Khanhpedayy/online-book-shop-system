package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndDeletedAtIsNull(Long userId);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.book
            WHERE o.id = :id
            """)
    Optional<Order> findDetailById(@Param("id") Long id);
}
