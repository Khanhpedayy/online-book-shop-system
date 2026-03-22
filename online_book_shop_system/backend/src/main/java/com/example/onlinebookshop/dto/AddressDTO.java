package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String label;
    private String recipientName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private boolean defaultAddress;
}
