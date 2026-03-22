package com.example.onlinebookshop.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
}
