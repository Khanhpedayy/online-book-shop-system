package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.entity.CartItem;
import com.example.onlinebookshop.service.CartService;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Get cart items for user.
     */
    @GetMapping("/user/{userId}")
    public List<CartItem> getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    /**
     * Add item to cart.
     */
    @PostMapping("/user/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addItem(@PathVariable Long userId, @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request);
    }

    /**
     * Update item quantity. Set quantity to 0 to remove.
     */
    @PutMapping("/user/{userId}/items/{variantId}")
    public CartItem updateItem(@PathVariable Long userId, @PathVariable Long variantId,
                               @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, variantId, request);
    }

    /**
     * Remove item from cart.
     */
    @DeleteMapping("/user/{userId}/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long userId, @PathVariable Long variantId) {
        cartService.removeItem(userId, variantId);
    }
}
