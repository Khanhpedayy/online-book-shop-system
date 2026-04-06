package com.example.onlinebookshop.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử giao dịch ví ảo.
 * type = CREDIT (tiền vào) | DEBIT (tiền ra khi tạo withdrawal)
 * ref_type = ORDER_REFUND | WITHDRAWAL
 */
@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 10)
    private String type;  // CREDIT | DEBIT

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "ref_type", length = 30)
    private String refType;  // ORDER_REFUND | WITHDRAWAL

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;
}
