package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;

import java.util.List;

public interface CartService {

    List<CartItem> getCartByEmail(String email);

    CartItem addItemByEmail(String email, AddToCartRequest request);

    CartItem updateItemByEmail(String email, Long variantId, UpdateCartItemRequest request);

    void removeItemByEmail(String email, Long variantId);
}
