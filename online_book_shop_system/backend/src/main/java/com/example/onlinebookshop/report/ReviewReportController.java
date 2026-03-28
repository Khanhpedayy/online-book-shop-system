package com.example.onlinebookshop.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.onlinebookshop.Repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Review Reports")
public class ReviewReportController {

    private final ReviewReportService service;
    private final UserRepository userRepository;

    public ReviewReportController(ReviewReportService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long uid(Authentication auth) {
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    /* ─── CUSTOMER ─── */

    /** POST /api/me/review-reports — gửi báo cáo */
    @PostMapping("/api/me/review-reports")
    @Operation(summary = "Gửi báo cáo review")
    public ResponseEntity<?> submit(@RequestBody ReviewReportDTOs.CreateReportRequest req,
                                    Authentication auth) {
        try {
            service.submitReport(uid(auth), req);
            return ResponseEntity.ok(Map.of("message", "Đã gửi báo cáo thành công!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /* ─── ADMIN ─── */

    /** GET /api/admin/review-reports?status=PENDING */
    @GetMapping("/api/admin/review-reports")
    @Operation(summary = "Admin: danh sách reports")
    public List<ReviewReportDTOs.ReportListDTO> getAll(
            @RequestParam(required = false) String status) {
        return service.getAll(status);
    }

    /** POST /api/admin/review-reports/{id}/decide */
    @PostMapping("/api/admin/review-reports/{id}/decide")
    @Operation(summary = "Admin: duyệt hoặc từ chối report")
    public ResponseEntity<?> decide(@PathVariable Long id,
                                    @RequestBody ReviewReportDTOs.AdminDecisionRequest req,
                                    Authentication auth) {
        try {
            if (req.isApprove()) {
                service.approve(id, uid(auth));
            } else {
                service.reject(id, uid(auth), req.getAdminNote());
            }
            return ResponseEntity.ok(Map.of("message", req.isApprove() ? "Đã xoá review" : "Đã từ chối báo cáo"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
