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

    private Long resolveUserIdByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCart(Long userId) {
        return cartItemRepository.findByUser_IdWithVariantAndBook(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartByEmail(String email) {
        return getCart(resolveUserIdByEmail(email));
    }

    @Override
    @Transactional
    public CartItem addItem(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        BookVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Book not found: " + request.getVariantId()));
        if (!variant.getIsActive() || (variant.getBook() != null && !"ACTIVE".equalsIgnoreCase(variant.getBook().getStatus()))) {
            throw new IllegalArgumentException("Book is not available: " + (variant.getBook() != null ? variant.getBook().getTitle() : variant.getSku()));
        }

        int qty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
        Long copyId = request.getCopyId();

        Optional<CartItem> existing = cartItemRepository.findByUser_IdAndVariant_IdAndCopyId(userId, request.getVariantId(), copyId);
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
            return cartItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public CartItem addItemByEmail(String email, AddToCartRequest request) {
        return addItem(resolveUserIdByEmail(email), request);
    }

    @Override
    @Transactional
    public CartItem updateItem(Long userId, Long variantId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByUser_IdAndVariant_IdAndCopyId(userId, variantId, null)
                .orElseThrow(() -> new RuntimeException("Cart item not found for variant: " + variantId));

        int qty = request.getQuantity() != null ? request.getQuantity() : item.getQuantity();
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return null;
        }
        item.setQuantity(qty);
        item.setUpdatedAt(LocalDateTime.now());
        return cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public CartItem updateItemByEmail(String email, Long variantId, UpdateCartItemRequest request) {
        return updateItem(resolveUserIdByEmail(email), variantId, request);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long variantId) {
        CartItem item = cartItemRepository.findByUser_IdAndVariant_IdAndCopyId(userId, variantId, null)
                .orElseThrow(() -> new RuntimeException("Cart item not found for variant: " + variantId));
        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void removeItemByEmail(String email, Long variantId) {
        removeItem(resolveUserIdByEmail(email), variantId);
    }
}
