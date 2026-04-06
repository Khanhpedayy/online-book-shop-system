package com.example.onlinebookshop.wallet;

import com.example.onlinebookshop.staff.service.StaffNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private static final BigDecimal DELIVERY_FAIL_FEE = new BigDecimal("10000");

    private final WalletRepository walletRepo;
    private final StaffNotificationService notificationService;

    public WalletService(WalletRepository walletRepo,
                         StaffNotificationService notificationService) {
        this.walletRepo = walletRepo;
        this.notificationService = notificationService;
    }

    // ─── Đọc thông tin ví ───────────────────────────────────────────

    public Map<String, Object> getWalletInfo(long userId) {
        BigDecimal balance = walletRepo.getBalance(userId);
        List<WalletRepository.WalletTxRow> txHistory = walletRepo.getTransactions(userId, 20);
        boolean hasPending = walletRepo.hasPendingWithdrawal(userId);
        List<WithdrawalRow> myWithdrawals = walletRepo.getWithdrawalsByUser(userId);
        return Map.of(
                "balance", balance,
                "transactions", txHistory,
                "hasPendingWithdrawal", hasPending,
                "withdrawals", myWithdrawals
        );
    }

    // ─── Credit ví khi giao hàng thất bại ────────────────────────────

    @Transactional
    public void creditOnDeliveryFailed(long userId, BigDecimal totalAmount, long orderId, String orderCode) {
        // Hoàn = total_amount - 10,000 phí ship
        BigDecimal refund = totalAmount.subtract(DELIVERY_FAIL_FEE);
        if (refund.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[WALLET] Refund amount <= 0 for orderId={}, skipping credit.", orderId);
            return;
        }

        BigDecimal newBalance = walletRepo.creditBalance(userId, refund);
        walletRepo.insertTransaction(
                userId, "CREDIT", refund, newBalance,
                "ORDER_REFUND", orderId,
                "Hoàn tiền đơn giao thất bại " + orderCode + " (đã trừ 10,000đ phí ship)"
        );

        // Gửi thông báo
        notificationService.notifyWalletCredited(userId, refund, orderCode);
        log.info("[WALLET] Credited {} VND to userId={} for failed delivery of order={}", refund, userId, orderCode);
    }

    // ─── Tạo yêu cầu rút tiền ────────────────────────────────────────

    @Transactional
    public WithdrawalRow createWithdrawalRequest(long userId, BigDecimal amount,
                                                  String bankName, String bankAccountNumber,
                                                  String bankAccountName) {
        // Validate
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền rút phải lớn hơn 0.");
        }
        if (bankName == null || bankName.isBlank()) throw new RuntimeException("Vui lòng nhập tên ngân hàng.");
        if (bankAccountNumber == null || bankAccountNumber.isBlank()) throw new RuntimeException("Vui lòng nhập số tài khoản.");
        if (bankAccountName == null || bankAccountName.isBlank()) throw new RuntimeException("Vui lòng nhập tên chủ tài khoản.");

        // Kiểm tra pending
        if (walletRepo.hasPendingWithdrawal(userId)) {
            throw new RuntimeException("Bạn đang có yêu cầu rút tiền chờ xử lý. Vui lòng đợi admin xử lý xong mới tạo yêu cầu mới.");
        }

        // Kiểm tra số dư — debitBalance sẽ throw nếu không đủ
        BigDecimal newBalance = walletRepo.debitBalance(userId, amount);

        // Ghi wallet transaction DEBIT
        String requestCode = generateRequestCode();
        Long txId = walletRepo.insertTransaction(
                userId, "DEBIT", amount, newBalance,
                "WITHDRAWAL", null,
                "Tạo yêu cầu rút tiền " + requestCode
        );

        // Ghi withdrawal request
        Long wrId = walletRepo.insertWithdrawalRequest(
                userId, amount,
                bankName.trim(), bankAccountNumber.trim(), bankAccountName.trim(),
                txId, requestCode
        );

        // Update ref_id của wallet_tx
        // (không cần thiết nếu không query theo; có thể bổ sung sau)

        return walletRepo.getWithdrawalsByUser(userId).stream()
                .filter(r -> r.getId().equals(wrId))
                .findFirst()
                .orElseThrow();
    }

    // ─── Admin: Duyệt yêu cầu ────────────────────────────────────────

    @Transactional
    public void approveWithdrawal(long requestId, long adminUserId) {
        WithdrawalRow request = walletRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền."));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Yêu cầu không còn ở trạng thái PENDING.");
        }

        int updated = walletRepo.updateWithdrawalStatus(requestId, "APPROVED", adminUserId, null);
        if (updated == 0) throw new RuntimeException("Không thể cập nhật trạng thái (đã bị thay đổi).");

        // Gửi thông báo
        notificationService.notifyWithdrawalApproved(request.getUserId(), request.getAmount());
        log.info("[WALLET] Withdrawal {} APPROVED by adminId={}", request.getRequestCode(), adminUserId);
    }

    // ─── Admin: Từ chối yêu cầu ──────────────────────────────────────

    @Transactional
    public void rejectWithdrawal(long requestId, long adminUserId, String reason) {
        WithdrawalRow request = walletRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền."));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Yêu cầu không còn ở trạng thái PENDING.");
        }

        int updated = walletRepo.updateWithdrawalStatus(requestId, "REJECTED", adminUserId,
                reason == null ? null : reason.trim());
        if (updated == 0) throw new RuntimeException("Không thể cập nhật trạng thái.");

        // Hoàn tiền lại ví
        BigDecimal newBalance = walletRepo.creditBalance(request.getUserId(), request.getAmount());
        walletRepo.insertTransaction(
                request.getUserId(), "CREDIT", request.getAmount(), newBalance,
                "WITHDRAWAL", requestId,
                "Hoàn tiền yêu cầu rút bị từ chối: " + request.getRequestCode()
        );

        // Gửi thông báo
        notificationService.notifyWithdrawalRejected(request.getUserId(), request.getAmount(), reason);
        log.info("[WALLET] Withdrawal {} REJECTED by adminId={}", request.getRequestCode(), adminUserId);
    }

    // ─── Admin: list pending ──────────────────────────────────────────

    public List<WithdrawalRow> getAllPendingWithdrawals() {
        return walletRepo.getAllPending();
    }

    /**
     * Lấy danh sách withdrawal theo status (null = tất cả).
     */
    public List<WithdrawalRow> getAll(String status) {
        return walletRepo.getAll(status);
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private String generateRequestCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "WD-" + datePart;
    }
}
