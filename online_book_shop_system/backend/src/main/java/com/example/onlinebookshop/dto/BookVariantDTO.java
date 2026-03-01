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
public class BookVariantDTO {
    private Long id;           // variant id
    private Long bookId;
    private String title;
    private String sku;
    private String isbn;
    private BigDecimal salePrice;
    private BigDecimal listPrice;
    private String description;
    private String status;
}
