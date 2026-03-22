package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
