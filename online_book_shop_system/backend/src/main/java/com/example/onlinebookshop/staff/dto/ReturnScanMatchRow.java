package com.example.onlinebookshop.staff.dto;

public class ReturnScanMatchRow {

    private Long orderId;
    private Long orderItemId;
    private Long copyId;
    private Long variantId;

    private String orderCode;
    private String titleSnapshot;
    private String skuSnapshot;
    private String copyCode;
    private String currentCopyStatus;
    private String currentConditionGrade;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getCopyId() { return copyId; }
    public void setCopyId(Long copyId) { this.copyId = copyId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getCopyCode() { return copyCode; }
    public void setCopyCode(String copyCode) { this.copyCode = copyCode; }

    public String getCurrentCopyStatus() { return currentCopyStatus; }
    public void setCurrentCopyStatus(String currentCopyStatus) { this.currentCopyStatus = currentCopyStatus; }

    public String getCurrentConditionGrade() { return currentConditionGrade; }
    public void setCurrentConditionGrade(String currentConditionGrade) { this.currentConditionGrade = currentConditionGrade; }
}