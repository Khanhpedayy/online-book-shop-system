package com.example.onlinebookshop.inventory;

import lombok.Data;

@Data
public class OverstockAlertDTO {
    private Long lotId;
    private String lotCode;
    private Long variantId;
    private String sku;
    private String title;
    private Integer qtyAvailable;
    private Long ageDays; // Days since received
}

