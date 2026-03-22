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
SELECT o FROM Order o
WHERE o.user.id = :userId
AND (:status IS NULL OR o.status = :status)
AND (:keyword IS NULL OR CAST(o.id AS string) LIKE %:keyword%)
ORDER BY o.createdAt DESC
""")
    List<Order> searchOrders(Long userId, String status, String keyword);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
