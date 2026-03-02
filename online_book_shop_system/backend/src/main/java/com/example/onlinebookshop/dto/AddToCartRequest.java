package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Long variantId;
    private Integer quantity;
    private Long copyId;     // NULL = buy by qty, NOT NULL = specific copy
}
