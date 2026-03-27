package com.example.onlinebookshop.dto;

import lombok.Data;

@Data
public class PayOSReturnSyncRequest {
    /**
     * PayOS paymentLinkId returned in payment-result.html query string (`id=`).
     */
    private String paymentLinkId;

    /**
     * Expected values: "PAID" or "UNPAID" (we normalize on backend).
     */
    private String targetStatus;
}

