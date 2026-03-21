package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.Service.CartService;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.UpdateCartItemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ✅ GET MY CART
    @GetMapping("/me")
    public List<CartItem> getMyCart(Authentication auth) {
        String email = auth.getName();
        return cartService.getCartByEmail(email);
    }

    // ✅ ADD ITEM
    @PostMapping("/items")
    public CartItem addToCart(@RequestBody AddToCartRequest req, Authentication auth) {
        String email = auth.getName();
        return cartService.addItemByEmail(email, req);
    }

    // ✅ UPDATE ITEM
    @PutMapping("/items/{variantId}")
    public CartItem updateItem(@PathVariable Long variantId,
                               @RequestBody UpdateCartItemRequest request,
                               Authentication auth) {
        String email = auth.getName();
        return cartService.updateItemByEmail(email, variantId, request);
    }

    // ✅ DELETE ITEM
    @DeleteMapping("/items/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long variantId, Authentication auth) {
        String email = auth.getName();
        cartService.removeItemByEmail(email, variantId);
    }
}