package com.example.onlinebookshop.wallet;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalRow {
    private Long id;
    private String requestCode;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    // admin view only
    private Long userId;
    private String userName;
    private String userEmail;
}
