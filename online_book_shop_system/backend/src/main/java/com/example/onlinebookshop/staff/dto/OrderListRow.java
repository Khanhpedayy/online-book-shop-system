package com.example.onlinebookshop.staff.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row DTO cho màn Order List (STAFF).
 */
public class OrderListRow {

    private Long id;
    private String orderCode;
    private String status;
    private String paymentStatus;

    private String shipName;
    private String shipPhone;

    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getShipPhone() { return shipPhone; }
    public void setShipPhone(String shipPhone) { this.shipPhone = shipPhone; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}