package com.example.onlinebookshop;

import com.example.onlinebookshop.dto.OrderRequest;

import java.util.List;

public interface OrderService {

    Order placeOrder(OrderRequest request);

    Order getOrderById(Long id);

    List<Order> getOrdersByCustomerId(Long customerId);
}
