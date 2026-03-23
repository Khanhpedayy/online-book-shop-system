package com.example.onlinebookshop.customer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CustomerDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private int orderCount;
    private String createdAt;
}
