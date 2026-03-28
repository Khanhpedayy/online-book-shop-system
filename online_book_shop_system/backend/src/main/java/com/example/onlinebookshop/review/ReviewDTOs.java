package com.example.onlinebookshop.review;

import lombok.*;

public class ReviewDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReviewRequest {
        private Long bookId;
        private Long orderId;
        private Integer rating;      // 1-5
        private String title;
        private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReviewResponse {
        private Long id;
        private Long bookId;
        private String bookTitle;
        private Long orderId;
        private Integer rating;
        private String title;
        private String content;
        private String status;
        private String createdAt;
        private String reviewerName;
    }

    /** Sách đã mua + trạng thái đã review chưa */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PurchasedBookDTO {
        private Long bookId;
        private String bookTitle;
        private String bookCoverUrl;
        private Long variantId;
        private String sku;
        private Long orderId;
        private String orderCode;
        private boolean reviewed;    // true nếu đã có review
        private Long reviewId;       // null nếu chưa review
        private Integer reviewRating;
    }
}
