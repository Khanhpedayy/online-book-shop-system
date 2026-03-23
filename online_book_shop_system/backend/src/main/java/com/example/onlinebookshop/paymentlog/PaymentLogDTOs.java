package com.example.onlinebookshop.paymentlog;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PaymentLogDTO {
    private Long id;
    private Long orderId;
    private String orderCode;
    private String provider;
    private String transactionId;
    private Double amount;
    private String status;
    private boolean flagged;
    private String flagReason;
    private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class FlagPaymentRequest {
    private String reason;
}
