package com.example.onlinebookshop.wallet;

import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

/**
 * Staff REST API for COD wallet.
 * All endpoints require STAFF role (enforced by SecurityConfig: /staff/** → hasAnyRole("STAFF")).
 */
@RestController
@RequestMapping("/staff/wallet")
public class CodWalletController {

    private final CodWalletService codWalletService;
    private final UserRepository userRepository;

    public CodWalletController(CodWalletService codWalletService, UserRepository userRepository) {
        this.codWalletService = codWalletService;
        this.userRepository = userRepository;
    }

    /** GET /staff/wallet/me — số dư + lịch sử giao dịch + deposits */
    @GetMapping("/me")
    public ResponseEntity<?> getMyWallet(Authentication auth) {
        Long staffId = resolveUserId(auth);
        if (staffId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));
        try {
            return ResponseEntity.ok(codWalletService.getWalletInfo(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** POST /staff/wallet/deposit — tạo PayOS QR nộp toàn bộ số dư */
    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(Authentication auth) {
        Long staffId = resolveUserId(auth);
        if (staffId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập."));
        try {
            Map<String, Object> result = codWalletService.createDeposit(staffId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** GET /staff/wallet/deposit/cancel — PayOS redirect khi staff bấm huỷ thanh toán */
    @GetMapping("/deposit/cancel")
    public RedirectView cancelDeposit(@RequestParam("depositCode") String depositCode, Authentication auth) {
        try {
            Long staffId = resolveUserId(auth);
            if (staffId != null) {
                codWalletService.cancelDeposit(staffId, depositCode);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CodWalletController.class)
                    .error("Failed to cancel deposit {}", depositCode, e);
        }
        return new RedirectView("/staff/workspace/dashboard?depositResult=cancelled");
    }

    /** GET /staff/wallet/deposit/success — PayOS redirect khi staff nộp tiền thành công (Fallback for localhost) */
    @GetMapping("/deposit/success")
    public RedirectView depositSuccess(
            @RequestParam("depositCode") String depositCode,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "status", required = false) String status,
            Authentication auth) {
        try {
            Long staffId = resolveUserId(auth);
            if (staffId != null && "00".equals(code) && "PAID".equals(status)) {
                codWalletService.handleDepositPaidFallback(staffId, depositCode);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CodWalletController.class)
                    .error("Failed to process fallback success for deposit {}", depositCode, e);
        }
        return new RedirectView("/staff/workspace/dashboard?depositResult=success");
    }

    /** GET /management/cod-deposits — Admin xem danh sách nộp tiền COD */
    @GetMapping("/admin/cod-deposits")
    public ResponseEntity<List<CodDepositRow>> adminList(
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(codWalletService.getAllDeposits(status));
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .map(u -> u.getId()).orElse(null);
    }
}
