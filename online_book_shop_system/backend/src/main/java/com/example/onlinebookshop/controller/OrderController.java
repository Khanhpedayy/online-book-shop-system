package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.OrderService;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    @PostMapping("/from-cart")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> checkoutFromCart(
            @RequestBody CheckoutRequest request,
            org.springframework.security.core.Authentication auth
    ) {
        String email = auth.getName();

        Order order = orderService.placeOrderFromCartByEmail(email, request);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("totalAmount", order.getTotalAmount());

        if ("PAYOS".equalsIgnoreCase(request.getPaymentMethod())) {
            String paymentUrl = orderService.createPayment(order);
            response.put("paymentUrl", paymentUrl);
        }

        return response;
    }


    @GetMapping("/me")
    public List<Order> getMyOrders(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        String email = auth.getName();
        return orderService.getOrdersByEmail(email, status, keyword, fromDate, toDate);
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

    @GetMapping("/{id}/me")
    public Order getMyOrder(@PathVariable Long id, Authentication auth) {
        return orderService.getOrderDetailByEmail(id, auth.getName());
    }

    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id, Authentication auth) {
        orderService.cancelOrder(id, auth.getName());
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/repay")
    public Map<String, Object> repay(@PathVariable Long id, Authentication auth) {
        Order order = orderService.getOrderDetailByEmail(id, auth.getName());

        if (order.getPaymentMethod() == null || !"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new IllegalStateException("Repay is only available for PayOS orders.");
        }

        String ps = order.getPaymentStatus();
        if (ps != null
                && !"PENDING".equalsIgnoreCase(ps)
                && !"UNPAID".equalsIgnoreCase(ps)
                && !"CANCELLED".equalsIgnoreCase(ps)) {
            throw new IllegalStateException("Order already paid or not eligible for repay.");
        }

        String paymentUrl = orderService.createPayment(order);

        return Map.of("paymentUrl", paymentUrl);
    }
}
