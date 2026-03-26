package com.example.onlinebookshop.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoint — no auth required.
 * GET /api/books/{id}/reviews
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Reviews (Public)")
public class BookReviewController {

    private final ReviewService service;

    public BookReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping("/{bookId}/reviews")
    @Operation(summary = "Lấy danh sách đánh giá của sách (public)")
    public List<ReviewDTOs.ReviewResponse> getReviews(@PathVariable Long bookId) {
        return service.getReviewsByBookId(bookId);
    }
}
