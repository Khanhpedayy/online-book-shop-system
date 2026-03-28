package com.example.onlinebookshop.review;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository repo;

    public ReviewService(ReviewRepository repo) {
        this.repo = repo;
    }

    /** Lấy tất cả review của 1 cuốn sách (public) */
    public List<ReviewDTOs.ReviewResponse> getReviewsByBookId(Long bookId) {
        return repo.findPublishedByBookId(bookId);
    }

    /** Lấy tất cả sách đã mua kèm trạng thái review */
    public List<ReviewDTOs.PurchasedBookDTO> getPurchasedBooks(Long userId) {
        return repo.findPurchasedBooks(userId);
    }

    /** Tạo review mới */
    public Long createReview(Long userId, ReviewDTOs.CreateReviewRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }
        if (!repo.hasPurchased(userId, req.getBookId())) {
            throw new IllegalStateException("Bạn chưa mua cuốn sách này");
        }
        if (repo.hasReviewed(userId, req.getBookId())) {
            throw new IllegalStateException("Bạn đã đánh giá cuốn sách này rồi");
        }
        return repo.createReview(userId, req);
    }

    /** Cập nhật review */
    public void updateReview(Long reviewId, Long userId, ReviewDTOs.CreateReviewRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }
        repo.updateReview(reviewId, userId, req);
    }

    /** Xóa review */
    public void deleteReview(Long reviewId, Long userId) {
        repo.deleteReview(reviewId, userId);
    }

    /** Lấy review của user cho 1 cuốn sách */
    public ReviewDTOs.ReviewResponse getReviewByBook(Long userId, Long bookId) {
        return repo.findByUserAndBook(userId, bookId);
    }
}
