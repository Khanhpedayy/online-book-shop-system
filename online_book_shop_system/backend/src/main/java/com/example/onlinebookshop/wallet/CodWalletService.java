package com.example.onlinebookshop.wallet;

import com.example.onlinebookshop.payos.PayOSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Manages the internal COD wallet for shippers (staff).
 *
 * Flow:
 *  1. COD order COMPLETED → creditShipperWallet() → wallet_balance += totalAmount
 *  2. Staff clicks "Nộp tiền" → createDeposit() → PayOS QR
 *  3. PayOS webhook confirms → handleDepositPaid() → wallet_balance -= amount
 */
@Service
public class CodWalletService {

    private static final Logger log = LoggerFactory.getLogger(CodWalletService.class);

    private final WalletRepository walletRepo;
    private final CodDepositRepository depositRepo;
    private final PayOSClient payOSClient;

    public CodWalletService(WalletRepository walletRepo,
                            CodDepositRepository depositRepo,
                            PayOSClient payOSClient) {
        this.walletRepo    = walletRepo;
        this.depositRepo   = depositRepo;
        this.payOSClient   = payOSClient;
    }

    // ─── 1. Credit on COD delivered ───────────────────────────────────

    /**
     * Credits the shipper's wallet with the COD amount collected.
     * Called when a COD order transitions to COMPLETED.
     *
     * @param staffId   ID of the shipper (from Authentication)
     * @param amount    order total_amount (full COD amount collected)
     * @param orderId   for audit trail
     * @param orderCode human-readable order code
     */
    @Transactional
    public void creditShipperWallet(long staffId, BigDecimal amount, long orderId, String orderCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[COD-WALLET] Skipping credit for orderId={}: amount <= 0", orderId);
            return;
        }
        BigDecimal newBalance = walletRepo.creditBalance(staffId, amount);
        walletRepo.insertTransaction(
                staffId, "CREDIT", amount, newBalance,
                "COD_COLLECT", orderId,
                "Thu COD đơn " + orderCode
        );
        log.info("[COD-WALLET] Credited {}đ to staffId={} for order={}", amount, staffId, orderCode);
    }

    // ─── 2. Create deposit request ────────────────────────────────────

    /**
     * Creates a COD deposit request and a PayOS payment link.
     * Deducts the full wallet balance immediately to "lock" it.
     *
     * @param staffId  the shipper's user ID
     * @return Map containing checkoutUrl and depositCode
     */
    @Transactional
    public Map<String, Object> createDeposit(long staffId) {
        BigDecimal balance = walletRepo.getBalance(staffId);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số dư ví bằng 0, không có tiền để nộp.");
        }
        if (depositRepo.hasPendingDeposit(staffId)) {
            throw new RuntimeException("Bạn đang có yêu cầu nộp tiền đang chờ xử lý. Vui lòng hoàn tất trước.");
        }

        // Debit the wallet immediately (lock the amount)
        BigDecimal newBalance = walletRepo.debitBalance(staffId, balance);
        String depositCode = "DEP-" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Record the debit transaction
        walletRepo.insertTransaction(
                staffId, "DEBIT", balance, newBalance,
                "COD_DEPOSIT", null,
                "Tạo yêu cầu nộp tiền COD " + depositCode
        );

        // Create PayOS payment link
        // Use depositCode as description (max 9 chars → truncate)
        String description = depositCode.length() > 9 ? depositCode.substring(depositCode.length() - 9) : depositCode;
        // Use a fake "orderId" = 0 so we can provide our own return URL
        String returnUrl = "http://localhost:8080/staff/wallet/deposit/success?depositCode=" + depositCode;
        String cancelUrl = "http://localhost:8080/staff/wallet/deposit/cancel?depositCode=" + depositCode;

        PayOSClient.PayOSCheckoutResult result = payOSClient.createCheckoutForDeposit(
                balance, description, returnUrl, cancelUrl);

        // Persist deposit record
        depositRepo.insert(staffId, balance, depositCode,
                result.payosOrderCode(), result.paymentLinkId(), result.checkoutUrl());

        log.info("[COD-WALLET] Deposit {} created for staffId={} amount={}", depositCode, staffId, balance);
        return Map.of(
                "depositCode", depositCode,
                "amount", balance,
                "checkoutUrl", result.checkoutUrl()
        );
    }

    // ─── 3. Handle PayOS webhook ──────────────────────────────────────

    /**
     * Called when PayOS webhook confirms deposit payment.
     * Marks deposit as PAID (wallet was already debited in createDeposit).
     */
    @Transactional
    public void handleDepositPaid(String payosLinkId) {
        CodDepositRow deposit = depositRepo.findByPayosLinkId(payosLinkId)
                .orElseThrow(() -> new RuntimeException("Deposit not found for payosLinkId=" + payosLinkId));

        if (!"PENDING".equals(deposit.getStatus())) {
            log.warn("[COD-WALLET] Deposit {} already in status {}, skipping", deposit.getDepositCode(), deposit.getStatus());
            return;
        }

        int updated = depositRepo.markPaid(deposit.getId());
        if (updated == 0) {
            throw new RuntimeException("Failed to mark deposit PAID: " + deposit.getDepositCode());
        }
        log.info("[COD-WALLET] Deposit {} PAID via WEBHOOK for staffId={}", deposit.getDepositCode(), deposit.getStaffId());
    }

    /**
     * Fallback if webhook is not reachable (e.g. localhost testing).
     * Synchronizes status via the Return URL redirection.
     */
    @Transactional
    public void handleDepositPaidFallback(Long staffId, String code) {
        CodDepositRow deposit = depositRepo.findByCodeAndStaff(code, staffId).orElse(null);
        if (deposit != null && "PENDING".equals(deposit.getStatus())) {
            depositRepo.markPaid(deposit.getId());
            log.info("[COD-WALLET] Deposit {} PAID via RETURN-URL (fallback) for staffId={}", code, staffId);
        }
    }

    /**
     * Called when staff manually cancels deposit from PayOS checkout page.
     */
    @Transactional
    public void cancelDeposit(Long staffId, String code) {
        CodDepositRow deposit = depositRepo.findByCodeAndStaff(code, staffId)
                .orElseThrow(() -> new RuntimeException("Deposit not found: " + code));

        if (!"PENDING".equals(deposit.getStatus())) {
            log.warn("[COD-WALLET] Deposit {} already {} cannot cancel", code, deposit.getStatus());
            return;
        }

        int updated = depositRepo.markCancelled(deposit.getId());
        if (updated > 0) {
            BigDecimal newBalance = walletRepo.creditBalance(staffId, deposit.getAmount());
            walletRepo.insertTransaction(
                    staffId, "CREDIT", deposit.getAmount(), newBalance,
                    "COD_DEPOSIT_CANCEL", null,
                    "Huỷ nộp tiền COD " + code
            );
            log.info("[COD-WALLET] Deposit {} CANCELLED, refunded {} to staffId={}", code, deposit.getAmount(), staffId);
        }
    }

    // ─── 4. Read wallet info ──────────────────────────────────────────

    public Map<String, Object> getWalletInfo(long staffId) {
        BigDecimal balance = walletRepo.getBalance(staffId);
        List<WalletRepository.WalletTxRow> transactions = walletRepo.getTransactions(staffId, 10);
        List<CodDepositRow> deposits = depositRepo.findByStaff(staffId);
        boolean hasPending = depositRepo.hasPendingDeposit(staffId);
        return Map.of(
                "balance", balance,
                "transactions", transactions,
                "deposits", deposits,
                "hasPendingDeposit", hasPending
        );
    }

    // ─── 5. Admin query ───────────────────────────────────────────────

    public List<CodDepositRow> getAllDeposits(String status) {
        return depositRepo.findAll(status);
    }

    // ─── Helpers ──────────────────────────────────────────────────────

}
