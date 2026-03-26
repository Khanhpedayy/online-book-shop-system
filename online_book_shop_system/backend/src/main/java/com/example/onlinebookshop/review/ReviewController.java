package com.example.onlinebookshop.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.onlinebookshop.Repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me/reviews")
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService service;
    private final UserRepository userRepository;

    public ReviewController(ReviewService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /** GET /api/me/reviews/purchased — danh sách sách đã mua + trạng thái review */
    @GetMapping("/purchased")
    @Operation(summary = "Sách đã mua kèm trạng thái review")
    public List<ReviewDTOs.PurchasedBookDTO> getPurchasedBooks(Authentication auth) {
        return service.getPurchasedBooks(getUserId(auth));
    }

    /** POST /api/me/reviews — tạo review mới */
    @PostMapping
    @Operation(summary = "Tạo đánh giá")
    public ResponseEntity<?> createReview(@RequestBody ReviewDTOs.CreateReviewRequest req,
                                          Authentication auth) {
        try {
            Long id = service.createReview(getUserId(auth), req);
            return ResponseEntity.ok(Map.of("id", id, "message", "Đánh giá thành công!"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** PUT /api/me/reviews/{id} — sửa review */
    @PutMapping("/{id}")
    @Operation(summary = "Sửa đánh giá")
    public ResponseEntity<?> updateReview(@PathVariable Long id,
                                          @RequestBody ReviewDTOs.CreateReviewRequest req,
                                          Authentication auth) {
        try {
            service.updateReview(id, getUserId(auth), req);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** DELETE /api/me/reviews/{id} — xóa review */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đánh giá")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication auth) {
        service.deleteReview(id, getUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá"));
    }
}
