package com.example.onlinebookshop.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yêu cầu rút tiền từ ví ảo.
 * Trạng thái: PENDING → APPROVED hoặc REJECTED
 * Tiền bị DEBIT khỏi ví ngay khi tạo request.
 * Nếu REJECTED → tiền được CREDIT lại.
 */
@Entity
@Table(name = "withdrawal_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", nullable = false, length = 60)
    private String requestCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", nullable = false, length = 150)
    private String bankAccountName;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";  // PENDING | APPROVED | REJECTED

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "wallet_tx_id")
    private Long walletTxId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
