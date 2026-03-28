package com.example.onlinebookshop.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.onlinebookshop.Repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService service;
    private final UserRepository userRepository;

    public NotificationController(NotificationService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long uid(Authentication auth) {
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả thông báo")
    public List<NotificationDTOs.NotificationDTO> getAll(Authentication auth) {
        return service.getAll(uid(auth));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Số thông báo chưa đọc")
    public NotificationDTOs.UnreadCountDTO unreadCount(Authentication auth) {
        return service.getUnreadCount(uid(auth));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        service.markRead(id, uid(auth));
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đọc"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả đã đọc")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        service.markAllRead(uid(auth));
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả"));
    }
}
