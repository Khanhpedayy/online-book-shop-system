package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String email;
    private String shippingAddress;
    private String recipientName;
    private Long customerId;  // user_id - required, must be logged in
}
