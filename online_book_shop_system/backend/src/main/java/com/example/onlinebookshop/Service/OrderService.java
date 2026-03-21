package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.CartItem;
import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderRequest;

import java.util.List;

public interface OrderService {



    Order placeOrder(OrderRequest request);

    Order placeOrderFromCartByEmail(String email, CheckoutRequest request);

    Order getOrderById(Long id);

    String createPayment(Order order);

    List<Order> getOrdersByCustomerId(Long customerId);
}
