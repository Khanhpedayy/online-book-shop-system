package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.BookVariantRepository;
import com.example.onlinebookshop.Repository.CartItemRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final BookVariantRepository variantRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartItemRepository cartItemRepository,
                           BookVariantRepository variantRepository,
                           UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    // ✅ GET CART
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartByEmail(String email) {
        User user = getUserByEmail(email);
        return cartItemRepository.findByUser_IdOrderByAddedAtDesc(user.getId());
    }

    // ✅ ADD ITEM
    @Override
    @Transactional
    public CartItem addItemByEmail(String email, AddToCartRequest request) {
        User user = getUserByEmail(email);

        BookVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Book not found: " + request.getVariantId()));

        if (!variant.getIsActive() ||
                (variant.getBook() != null && !"ACTIVE".equalsIgnoreCase(variant.getBook().getStatus()))) {
            throw new IllegalArgumentException("Book is not available");
        }

        int qty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        Long copyId = request.getCopyId();

        Optional<CartItem> existing = copyId == null
                ? cartItemRepository.findByUser_IdAndVariant_IdAndCopyIdIsNull(user.getId(), request.getVariantId())
                : cartItemRepository.findByUser_IdAndVariant_IdAndCopyId(user.getId(), request.getVariantId(), copyId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + qty);
            item.setUpdatedAt(LocalDateTime.now());
            return cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setUser(user);
            item.setVariant(variant);
            item.setCopyId(copyId);
            item.setQuantity(qty);
            item.setAddedAt(LocalDateTime.now());
            return cartItemRepository.save(item);
        }
    }

    // ✅ UPDATE ITEM
    @Override
    @Transactional
    public CartItem updateItemByEmail(String email, Long variantId, UpdateCartItemRequest request) {
        User user = getUserByEmail(email);

        CartItem item = cartItemRepository
                .findByUser_IdAndVariant_IdAndCopyIdIsNull(user.getId(), variantId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int qty = request.getQuantity() != null ? request.getQuantity() : item.getQuantity();

        if (qty <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        item.setQuantity(qty);
        item.setUpdatedAt(LocalDateTime.now());
        return cartItemRepository.save(item);
    }

    // ✅ DELETE ITEM
    @Override
    @Transactional
    public void removeItemByEmail(String email, Long variantId) {
        User user = getUserByEmail(email);

        CartItem item = cartItemRepository
                .findByUser_IdAndVariant_IdAndCopyIdIsNull(user.getId(), variantId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItemRepository.delete(item);
    }
}
