package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser_IdOrderByAddedAtDesc(Long userId);
    Optional<CartItem> findByUser_IdAndVariant_IdAndCopyId(Long userId, Long variantId, Long copyId);
}
