package com.example.onlinebookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long variantId;
    private Long bookId;
    private Long copyId;
    private int quantity;
    private String title;
    private String sku;
    private BigDecimal salePrice;
    private String coverImageUrl;
}
