package com.example.onlinebookshop.staff.dto;

import java.time.LocalDateTime;

public class StaffAlert {

    private String severity;   // INFO / WARN / CRITICAL
    private String type;       // OVERDUE / PAYMENT_MISMATCH / STOCK_ISSUE
    private String message;

    private Long orderId;
    private String orderCode;
    private LocalDateTime createdAt;

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}