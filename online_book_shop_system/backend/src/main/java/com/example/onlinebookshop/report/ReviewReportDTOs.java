package com.example.onlinebookshop.report;

import lombok.*;

public class ReviewReportDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateReportRequest {
        private Long reviewId;
        private String reason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ReportListDTO {
        private Long id;
        private Long reviewId;
        private String reviewContent;
        private String reviewTitle;
        private Integer reviewRating;
        private String reviewerName;    // người viết review
        private String reporterName;    // người báo cáo
        private Long reporterId;
        private String reason;
        private String status;          // PENDING | APPROVED | REJECTED
        private String adminNote;
        private String createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AdminDecisionRequest {
        private boolean approve;        // true = xoá review, false = từ chối
        private String adminNote;       // bắt buộc nếu reject
    }
}
