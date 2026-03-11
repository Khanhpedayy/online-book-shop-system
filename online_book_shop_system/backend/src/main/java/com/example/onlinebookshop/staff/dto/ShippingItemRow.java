package com.example.onlinebookshop.staff.dto;

public class ShippingItemRow {
    private Long orderItemId;
    private String skuSnapshot;
    private String titleSnapshot;

    private Long copyId;
    private String copyCode;
    private String location;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public Long getCopyId() { return copyId; }
    public void setCopyId(Long copyId) { this.copyId = copyId; }

    public String getCopyCode() { return copyCode; }
    public void setCopyCode(String copyCode) { this.copyCode = copyCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}