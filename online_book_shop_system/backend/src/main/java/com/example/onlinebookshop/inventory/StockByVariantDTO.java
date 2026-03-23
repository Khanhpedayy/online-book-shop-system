package com.example.onlinebookshop.inventory;

import lombok.Data;

@Data
public class StockByVariantDTO {
    private Long variantId;
    private String sku;
    private String title;
    private String format;
    private Integer totalAvailable;
}

