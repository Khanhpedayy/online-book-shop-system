package com.example.onlinebookshop.repository;

import com.example.onlinebookshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndDeletedAtIsNull(Long userId);
}
