package com.example.onlinebookshop.staff.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturnIntakeView {
    private Long returnId;
    private String returnCode;
    private String status;
    private LocalDateTime receivedAt;

    private Long orderId;
    private String orderCode;
    private String shipName;
    private String shipPhone;

    private String reason;
    private String note;

    private List<OrderItemRow> orderItems = new ArrayList<>();
    private List<ReturnItemRow> scannedItems = new ArrayList<>();

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }

    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getShipPhone() { return shipPhone; }
    public void setShipPhone(String shipPhone) { this.shipPhone = shipPhone; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<OrderItemRow> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRow> orderItems) { this.orderItems = orderItems; }

    public List<ReturnItemRow> getScannedItems() { return scannedItems; }
    public void setScannedItems(List<ReturnItemRow> scannedItems) { this.scannedItems = scannedItems; }
}