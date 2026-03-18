package com.example.onlinebookshop.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String email;
    private String shippingAddress;
    private String recipientName;
    private String phone;
    private Long customerId;        // required - guests must log in to checkout
    private String paymentMethod;
}
