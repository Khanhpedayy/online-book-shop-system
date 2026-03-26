package com.example.onlinebookshop.staff.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer itemCount;
    private Integer allocatedCount;
    private Integer pickedCount;
    private List<AllocatePreviewRow> previewItems = new ArrayList<>();

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

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    public Integer getAllocatedCount() { return allocatedCount; }
    public void setAllocatedCount(Integer allocatedCount) { this.allocatedCount = allocatedCount; }

    public Integer getPickedCount() { return pickedCount; }
    public void setPickedCount(Integer pickedCount) { this.pickedCount = pickedCount; }

    public List<AllocatePreviewRow> getPreviewItems() { return previewItems; }
    public void setPreviewItems(List<AllocatePreviewRow> previewItems) { this.previewItems = previewItems; }
}