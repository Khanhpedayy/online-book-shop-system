package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StaffOrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT COUNT(*) FROM dbo.[orders] WHERE deleted_at IS NULL AND status = 'NEW'", nativeQuery = true)
    long countNewOrders();

    @Query(value = "SELECT COUNT(*) FROM dbo.[orders] WHERE deleted_at IS NULL AND payment_status = 'PENDING'", nativeQuery = true)
    long countPendingPayments();

    @Query(value = "SELECT COUNT(*) FROM dbo.[orders] WHERE deleted_at IS NULL AND status = 'CONFIRMED'", nativeQuery = true)
    long countToPack();

    @Query(value = """
            SELECT COUNT(*)
            FROM dbo.[orders]
            WHERE deleted_at IS NULL
              AND shipped_at >= ?1 AND shipped_at < ?2
            """, nativeQuery = true)
    long countShippedBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT COUNT(*)
            FROM dbo.[orders]
            WHERE deleted_at IS NULL
              AND status IN ('NEW','CONFIRMED')
              AND placed_at < ?1
            """, nativeQuery = true)
    long countOverdueBefore(LocalDateTime overdueThreshold);

    @Query(value = """
            SELECT TOP (10) *
            FROM dbo.[orders]
            WHERE deleted_at IS NULL
              AND status IN ('NEW','CONFIRMED')
            ORDER BY placed_at ASC
            """, nativeQuery = true)
    List<Order> findTodoTop10();
}