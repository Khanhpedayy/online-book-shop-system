package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String email;
    private String shippingAddress;
    private String recipientName;
    private Long customerId;        // required - guests must log in to checkout
}
