package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Long variantId;  // book_variants.id (was bookId)
    private Integer quantity;
}
