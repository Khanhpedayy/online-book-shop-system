package com.example.onlinebookshop.staffdashboard;

import lombok.*;
import java.util.List;

/* ═══ KPIs ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class StaffKpiDTO {
    private int newOrders;
    private int pendingPayments;
    private int ordersToPack;
    private int shippedToday;
}

/* ═══ To-do item ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TodoItemDTO {
    private Long orderId;
    private String orderCode;
    private String status;
    private String customerName;
    private int priority;
    private String slaDeadline;
    private String action; // CONFIRM, PICK, PACK, SHIP
    private String placedAt;
}

/* ═══ Alert ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AlertDTO {
    private String type; // OVERDUE_ORDER, PAYMENT_MISMATCH, LOW_STOCK
    private String severity; // HIGH, MEDIUM, LOW
    private String message;
    private Long referenceId;
    private String referenceCode;
    private String createdAt;
}
