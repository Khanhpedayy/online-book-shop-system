package com.example.onlinebookshop.support;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.onlinebookshop.Repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Support Tickets")
public class SupportTicketController {

    private final SupportTicketService service;
    private final UserRepository userRepository;

    public SupportTicketController(SupportTicketService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long uid(Authentication auth) {
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    private String uname(Authentication auth) {
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .map(u -> u.getFullName() != null ? u.getFullName() : auth.getName())
                .orElse(auth.getName());
    }

    /* ─── CUSTOMER ─── */

    @PostMapping("/api/me/support-tickets")
    @Operation(summary = "Tạo support ticket")
    public ResponseEntity<?> create(@RequestBody SupportTicketDTOs.CreateTicketRequest req,
                                    Authentication auth) {
        try {
            service.createTicket(uid(auth), req);
            return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu hỗ trợ thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/me/support-tickets")
    @Operation(summary = "Danh sách ticket của tôi")
    public List<SupportTicketDTOs.TicketSummaryDTO> myTickets(Authentication auth) {
        return service.getMyTickets(uid(auth));
    }

    @GetMapping("/api/me/support-tickets/{id}")
    @Operation(summary = "Chi tiết ticket")
    public ResponseEntity<?> myTicketDetail(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(service.getTicketDetail(id, uid(auth)));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ─── ADMIN ─── */

    @GetMapping("/api/admin/support-tickets")
    @Operation(summary = "Admin: danh sách tickets")
    public List<SupportTicketDTOs.TicketSummaryDTO> adminList(
            @RequestParam(required = false) String status) {
        return service.getAllTickets(status);
    }

    @GetMapping("/api/admin/support-tickets/{id}")
    @Operation(summary = "Admin: chi tiết ticket")
    public ResponseEntity<?> adminDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getAdminTicketDetail(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/admin/support-tickets/{id}/reply")
    @Operation(summary = "Admin: phản hồi ticket")
    public ResponseEntity<?> adminReply(@PathVariable Long id,
                                        @RequestBody SupportTicketDTOs.AdminReplyRequest req,
                                        Authentication auth) {
        try {
            service.adminReply(id, uname(auth), req);
            return ResponseEntity.ok(Map.of("message", "Đã gửi phản hồi!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
