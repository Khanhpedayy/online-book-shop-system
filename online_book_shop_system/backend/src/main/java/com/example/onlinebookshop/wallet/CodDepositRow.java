package com.example.onlinebookshop.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection row for cod_deposits table (admin + staff views).
 */
public class CodDepositRow {

    private Long id;
    private String depositCode;
    private Long staffId;
    private String staffName;
    private String staffEmail;
    private BigDecimal amount;
    private String status;
    private String checkoutUrl;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    // ─── Getters & Setters ─────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDepositCode() { return depositCode; }
    public void setDepositCode(String depositCode) { this.depositCode = depositCode; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffEmail() { return staffEmail; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
