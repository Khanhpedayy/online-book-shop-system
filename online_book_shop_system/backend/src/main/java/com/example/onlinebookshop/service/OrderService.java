package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;

import java.util.List;

public interface OrderService {

    Order placeOrder(OrderRequest request);

    Order placeOrderFromCart(Long userId, CheckoutRequest request);

    Order placeOrderFromCartByEmail(String email, CheckoutRequest request);

    String createPayment(Order order);

    Order getOrderById(Long id);

    Order getOrderDetailByEmail(Long id, String email);

    List<Order> getOrdersByCustomerId(Long customerId);

    List<Order> getOrdersByEmail(String email, String status, String keyword, String fromDate, String toDate);

    void cancelOrder(Long id, String email);
}
