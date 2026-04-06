package com.example.onlinebookshop.wallet;

import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin REST API để quản lý yêu cầu rút tiền.
 * Endpoint: /management/withdrawals
 */
@RestController
@RequestMapping("/management/withdrawals")
public class AdminWithdrawalController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public AdminWithdrawalController(WalletService walletService, UserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    /** GET /management/withdrawals?status=PENDING — lọc theo trạng thái (mặc định: PENDING) */
    @GetMapping
    public ResponseEntity<List<WithdrawalRow>> getByStatus(
            @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(walletService.getAll(status));
    }

    /** GET /management/withdrawals/all — lấy tất cả mọi trạng thái */
    @GetMapping("/all")
    public ResponseEntity<List<WithdrawalRow>> getAll() {
        return ResponseEntity.ok(walletService.getAll(null));
    }

    /** POST /management/withdrawals/{id}/approve */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable long id, Authentication auth) {
        Long adminId = resolveUserId(auth);
        if (adminId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));
        try {
            walletService.approveWithdrawal(id, adminId);
            return ResponseEntity.ok(Map.of("message", "Đã duyệt yêu cầu rút tiền."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** POST /management/withdrawals/{id}/reject */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable long id,
                                     @RequestBody(required = false) Map<String, String> body,
                                     Authentication auth) {
        Long adminId = resolveUserId(auth);
        if (adminId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));
        String reason = body != null ? body.get("reason") : null;
        try {
            walletService.rejectWithdrawal(id, adminId, reason);
            return ResponseEntity.ok(Map.of("message", "Đã từ chối và hoàn tiền về ví khách."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .map(u -> u.getId())
                .orElse(null);
    }
}
