package com.example.onlinebookshop.staff.dto;

import java.util.ArrayList;
import java.util.List;

public class PickListView {
    private Long orderId;
    private String orderCode;
    private String status;

    private int totalAllocated;
    private int totalPicked;

    private List<PickListItemRow> items = new ArrayList<>();

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalAllocated() { return totalAllocated; }
    public void setTotalAllocated(int totalAllocated) { this.totalAllocated = totalAllocated; }

    public int getTotalPicked() { return totalPicked; }
    public void setTotalPicked(int totalPicked) { this.totalPicked = totalPicked; }

    public List<PickListItemRow> getItems() { return items; }
    public void setItems(List<PickListItemRow> items) { this.items = items; }
}