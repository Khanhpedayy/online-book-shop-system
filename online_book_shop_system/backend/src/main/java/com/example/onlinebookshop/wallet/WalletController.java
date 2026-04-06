package com.example.onlinebookshop.wallet;

import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Customer-facing REST API cho ví ảo.
 * Tất cả endpoint đều yêu cầu đăng nhập (CUSTOMER).
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public WalletController(WalletService walletService, UserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    /** GET /api/wallet/me — Số dư + lịch sử giao dịch + danh sách withdrawal */
    @GetMapping("/me")
    public ResponseEntity<?> getWalletInfo(Authentication auth) {
        Long userId = resolveUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));
        return ResponseEntity.ok(walletService.getWalletInfo(userId));
    }

    /** POST /api/wallet/withdrawal — Tạo yêu cầu rút tiền */
    @PostMapping("/withdrawal")
    public ResponseEntity<?> createWithdrawal(@RequestBody WithdrawalRequest req,
                                               Authentication auth) {
        Long userId = resolveUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));

        try {
            WithdrawalRow result = walletService.createWithdrawalRequest(
                    userId,
                    req.getAmount(),
                    req.getBankName(),
                    req.getBankAccountNumber(),
                    req.getBankAccountName()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Helper ────────────────────────────────────────────────────

    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .map(u -> u.getId())
                .orElse(null);
    }
}
