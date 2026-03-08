package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndDeletedAtIsNull(Long userId);

    List<Order> findByStatusInAndDeletedAtIsNullOrderByPlacedAtDesc(List<String> statuses);

    List<Order> findByDeletedAtIsNullOrderByPlacedAtDesc();

    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
}
