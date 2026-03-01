package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;

import java.util.List;

public interface OrderService {

    Order placeOrder(OrderRequest request);

    Order placeOrderFromCart(Long userId, CheckoutRequest request);

    Order getOrderById(Long id);

    List<Order> getOrdersByCustomerId(Long customerId);
}
