package com.example.onlinebookshop.stock;

import lombok.*;

import java.util.List;

/* ═══ Stock Item (book + qty) ═══ */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class StockItemDTO {
    private Long bookId;
    private String title;
    private String isbn13;
    private String categoryName;
    private String status;
    private String coverImageUrl;
    private int stockQuantity;
}

/* ═══ Stock Adjustment Record ═══ */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class StockAdjustmentDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String adjustmentType;  // IMPORT, EXPORT, SET, DAMAGE, RETURN
    private int quantity;
    private int oldQuantity;
    private int newQuantity;
    private String reason;
    private String note;
    private String createdAt;
}

/* ═══ Update Stock Request (direct set) ═══ */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class UpdateStockRequest {
    private int quantity;
    private String note;
}

/* ═══ Adjust Stock Request (import/export) ═══ */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class AdjustStockRequest {
    private String type;      // IMPORT, EXPORT, DAMAGE, RETURN
    private int quantity;     // always positive; sign determined by type
    private String reason;
    private String note;
}
