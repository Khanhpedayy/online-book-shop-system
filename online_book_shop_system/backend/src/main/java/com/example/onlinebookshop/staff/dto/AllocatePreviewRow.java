package com.example.onlinebookshop.staff.dto;

public class AllocatePreviewRow {

    private Long orderItemId;
    private Long variantId;
    private String titleSnapshot;
    private String skuSnapshot;

    private Long copyId;
    private String copyCode;
    private String location;
    private String conditionGrade;

    private String sellMode;
    private String stockStatus;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public Long getCopyId() { return copyId; }
    public void setCopyId(Long copyId) { this.copyId = copyId; }

    public String getCopyCode() { return copyCode; }
    public void setCopyCode(String copyCode) { this.copyCode = copyCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getConditionGrade() { return conditionGrade; }
    public void setConditionGrade(String conditionGrade) { this.conditionGrade = conditionGrade; }

    public String getSellMode() { return sellMode; }
    public void setSellMode(String sellMode) { this.sellMode = sellMode; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}