package com.example.onlinebookshop.staff.dto;

import java.time.LocalDateTime;

public class PickListItemRow {
    private Long orderItemId;

    private String skuSnapshot;
    private String titleSnapshot;

    private Long copyId;
    private String copyCode;
    private String location;
    private String copyStatus;

    private LocalDateTime pickedAt;
    private String pickMethod;

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

    public String getCopyStatus() { return copyStatus; }
    public void setCopyStatus(String copyStatus) { this.copyStatus = copyStatus; }

    public LocalDateTime getPickedAt() { return pickedAt; }
    public void setPickedAt(LocalDateTime pickedAt) { this.pickedAt = pickedAt; }

    public String getPickMethod() { return pickMethod; }
    public void setPickMethod(String pickMethod) { this.pickMethod = pickMethod; }
}