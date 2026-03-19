package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.OrderService;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Checkout from cart. Converts cart items to order and clears cart.
     */
    @PostMapping("/from-cart/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> checkoutFromCart(
            @PathVariable Long userId,
            @RequestBody CheckoutRequest request
    ) {
        Order order = orderService.placeOrderFromCart(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("totalAmount", order.getTotalAmount());

        // 👇 xử lý payment luôn tại đây
        if ("PAYOS".equalsIgnoreCase(request.getPaymentMethod())) {
            String paymentUrl = orderService.createPayment(order);
            response.put("paymentUrl", paymentUrl);
        }

        return response;
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomerId(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomerId(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> placeOrder(@RequestBody OrderRequest request) {

        Order order = orderService.placeOrder(request);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());

        // nếu PayOS → trả payment URL
        if ("PAYOS".equalsIgnoreCase(request.getPaymentMethod())) {
            String paymentUrl = orderService.createPayment(order);
            response.put("paymentUrl", paymentUrl);
        }

        return response;
    }

    @PostMapping("/{id}/repay")
    public Map<String, Object> repay(@PathVariable Long id) {

        Order order = orderService.getOrderById(id);

        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order already paid or not eligible!");
        }

        String paymentUrl = orderService.createPayment(order);

        return Map.of("paymentUrl", paymentUrl);
    }
}
