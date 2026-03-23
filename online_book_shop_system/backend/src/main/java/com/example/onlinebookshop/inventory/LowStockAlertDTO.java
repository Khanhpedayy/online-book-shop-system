package com.example.onlinebookshop.inventory;

import lombok.Data;

@Data
public class LowStockAlertDTO {
    private Long variantId;
    private String sku;
    private String title;
    private Integer totalAvailable;
    private Integer threshold;
}

