package com.example.onlinebookshop.inventory;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ManagerStockByLotDTO {
    private Long lotId;
    private String lotCode;
    private Long variantId;
    private String sku;
    private String title;
    private Integer qtyReceived;
    private Integer qtyAvailable;
    private String conditionDefault;
    private LocalDateTime receivedAt;
}

