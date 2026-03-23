package com.example.onlinebookshop.returnsdecision;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReturnOverviewDTO {
    private Long returnId;
    private String returnCode;
    private Long orderId;
    private String orderCode;
    private String status;
    private String reason;
    private String note;
    private Double refundAmount;
    private String requestedBy;
    private String approvedBy;
    private String createdAt;
    private List<ReturnItemDTO> items;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReturnItemDTO {
    private Long id;
    private Long returnId;
    private Long orderItemId;
    private Long copyId;
    private String copyCode;
    private int quantity;
    private String receivedConditionGrade;
    private String receivedConditionNote;
    private String action;
    private String processedBy;
    private String processedAt;
    private String titleSnapshot;
    private String skuSnapshot;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ProcessReturnItemRequest {
    private String action; // RESTOCK, RESTOCK_REPRICE, DAMAGED, SUPPLIER_RETURN
    private String conditionGrade; // for RESTOCK_REPRICE
    private Double newSellPrice; // for RESTOCK_REPRICE
    private String note;
}
