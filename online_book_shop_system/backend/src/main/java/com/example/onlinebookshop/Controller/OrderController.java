package com.example.onlinebookshop.Controller;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.OrderService;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place order - supports both guest and customer.
     * Guest: provide email, shippingAddress, recipientName, items.
     * Customer: optionally provide customerId (can be added when auth exists).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    /**
     * Checkout from cart. Converts cart items to order and clears cart.
     */
    @PostMapping("/from-cart/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkoutFromCart(@PathVariable Long userId, @RequestBody CheckoutRequest request) {
        return orderService.placeOrderFromCart(userId, request);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomerId(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomerId(customerId);
    }
}
