package com.example.onlinebookshop.staff.dto;

import java.time.LocalDateTime;

public class ReturnItemRow {
    private Long id;
    private Long orderItemId;
    private Long copyId;
    private String copyCode;

    private String skuSnapshot;
    private String titleSnapshot;

    private String receivedConditionGrade;
    private String receivedConditionNote;

    private String action; // manager quyết sau
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getCopyId() { return copyId; }
    public void setCopyId(Long copyId) { this.copyId = copyId; }

    public String getCopyCode() { return copyCode; }
    public void setCopyCode(String copyCode) { this.copyCode = copyCode; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public String getReceivedConditionGrade() { return receivedConditionGrade; }
    public void setReceivedConditionGrade(String receivedConditionGrade) { this.receivedConditionGrade = receivedConditionGrade; }

    public String getReceivedConditionNote() { return receivedConditionNote; }
    public void setReceivedConditionNote(String receivedConditionNote) { this.receivedConditionNote = receivedConditionNote; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}