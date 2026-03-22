package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;

import java.util.List;

public interface CartService {

    List<CartItem> getCart(Long userId);

    List<CartItem> getCartByEmail(String email);

    CartItem addItem(Long userId, AddToCartRequest request);

    CartItem addItemByEmail(String email, AddToCartRequest request);

    CartItem updateItem(Long userId, Long variantId, UpdateCartItemRequest request);

    CartItem updateItemByEmail(String email, Long variantId, UpdateCartItemRequest request);

    void removeItem(Long userId, Long variantId);

    void removeItemByEmail(String email, Long variantId);
}
