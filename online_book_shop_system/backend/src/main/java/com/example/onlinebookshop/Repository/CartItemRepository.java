package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c JOIN FETCH c.variant v JOIN FETCH v.book WHERE c.user.id = :userId ORDER BY c.addedAt DESC")
    List<CartItem> findByUser_IdOrderByAddedAtDesc(Long userId);
    Optional<CartItem> findByUser_IdAndVariant_IdAndCopyId(Long userId, Long variantId, Long copyId);
    /** Use when copyId is null (quantity-based cart item) */
    Optional<CartItem> findByUser_IdAndVariant_IdAndCopyIdIsNull(Long userId, Long variantId);
    List<CartItem> findByUser_Id(Long userId);
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(Long userId);
}
