package com.example.onlinebookshop.staff.dto;

import java.math.BigDecimal;

/**
 * Item DTO cho màn Order Detail (STAFF).
 */
public class OrderItemRow {

    private Long id;
    private String skuSnapshot;
    private String titleSnapshot;
    private String conditionSnapshot;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Long copyId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSkuSnapshot() { return skuSnapshot; }
    public void setSkuSnapshot(String skuSnapshot) { this.skuSnapshot = skuSnapshot; }

    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }

    public String getConditionSnapshot() { return conditionSnapshot; }
    public void setConditionSnapshot(String conditionSnapshot) { this.conditionSnapshot = conditionSnapshot; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Long getCopyId() { return copyId; }
    public void setCopyId(Long copyId) { this.copyId = copyId; }

    public Long getOrderItemId() {
        return this.id;
    }
}