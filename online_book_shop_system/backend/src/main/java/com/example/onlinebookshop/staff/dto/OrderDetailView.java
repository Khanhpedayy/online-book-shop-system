package com.example.onlinebookshop.staff.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * View model cho màn Order Detail (STAFF).
 */
public class OrderDetailView {

    private Long id;
    private String orderCode;
    private Long userId;

    private String status;
    private String paymentStatus;

    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String shipName;
    private String shipPhone;
    private String shipLine1;
    private String shipLine2;
    private String shipWard;
    private String shipDistrict;
    private String shipCity;
    private String shipProvince;
    private String shipMethod;

    private String carrier;
    private String trackingCode;

    private String customerNote;
    private String staffNote;

    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private final List<OrderItemRow> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getShipPhone() { return shipPhone; }
    public void setShipPhone(String shipPhone) { this.shipPhone = shipPhone; }

    public String getShipLine1() { return shipLine1; }
    public void setShipLine1(String shipLine1) { this.shipLine1 = shipLine1; }

    public String getShipLine2() { return shipLine2; }
    public void setShipLine2(String shipLine2) { this.shipLine2 = shipLine2; }

    public String getShipWard() { return shipWard; }
    public void setShipWard(String shipWard) { this.shipWard = shipWard; }

    public String getShipDistrict() { return shipDistrict; }
    public void setShipDistrict(String shipDistrict) { this.shipDistrict = shipDistrict; }

    public String getShipCity() { return shipCity; }
    public void setShipCity(String shipCity) { this.shipCity = shipCity; }

    public String getShipProvince() { return shipProvince; }
    public void setShipProvince(String shipProvince) { this.shipProvince = shipProvince; }

    public String getShipMethod() { return shipMethod; }
    public void setShipMethod(String shipMethod) { this.shipMethod = shipMethod; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }

    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }

    public String getStaffNote() { return staffNote; }
    public void setStaffNote(String staffNote) { this.staffNote = staffNote; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getPackedAt() { return packedAt; }
    public void setPackedAt(LocalDateTime packedAt) { this.packedAt = packedAt; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public List<OrderItemRow> getItems() { return items; }
}