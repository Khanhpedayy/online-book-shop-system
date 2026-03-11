package com.example.onlinebookshop.staff.dto;

public class ReturnCreateForm {
    private Long orderId;
    private String reason;
    private String note;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}