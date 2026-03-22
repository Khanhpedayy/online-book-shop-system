package com.example.onlinebookshop.dto;

import lombok.Data;

@Data
public class AddressRequest {
    private String label;
    private String recipientName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private Boolean defaultAddress;
}
