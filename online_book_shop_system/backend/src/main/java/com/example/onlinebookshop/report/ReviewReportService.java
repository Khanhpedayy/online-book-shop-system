package com.example.onlinebookshop.report;

import com.example.onlinebookshop.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewReportService {

    private final ReviewReportRepository repo;
    private final NotificationService notifService;

    public ReviewReportService(ReviewReportRepository repo, NotificationService notifService) {
        this.repo = repo;
        this.notifService = notifService;
    }

    /** Customer gửi báo cáo */
    public void submitReport(Long reporterId, ReviewReportDTOs.CreateReportRequest req) {
        if (req.getReviewId() == null || req.getReason() == null || req.getReason().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do báo cáo");
        }

        // Không được tự báo cáo review của mình
        Long authorId = repo.getReviewAuthorId(req.getReviewId());
        if (authorId == null) {
            throw new IllegalStateException("Bài đánh giá không tồn tại");
        }
        if (authorId.equals(reporterId)) {
            throw new IllegalStateException("Bạn không thể báo cáo đánh giá của chính mình");
        }

        // Đã báo cáo review này chưa?
        if (repo.hasReported(reporterId, req.getReviewId())) {
            throw new IllegalStateException("Bạn đã báo cáo đánh giá này rồi");
        }

        repo.create(reporterId, req);
    }

    /** Admin: lấy danh sách reports */
    public List<ReviewReportDTOs.ReportListDTO> getAll(String status) {
        return repo.findAll(status);
    }

    /** Admin: đồng ý xoá review */
    public void approve(Long reportId, Long adminId) {
        ReviewReportDTOs.ReportListDTO report = repo.findById(reportId);
        if (report == null) throw new IllegalStateException("Report không tồn tại");

        repo.approve(reportId, adminId);

        // Notify reviewer (review bị xoá)
        Long reviewerId = repo.getReviewAuthorId(report.getReviewId());
        if (reviewerId != null) {
            notifService.send(
                reviewerId,
                "Đánh giá của bạn đã bị xoá",
                "Đánh giá của bạn đã bị admin xoá do vi phạm chính sách cộng đồng.",
                "REVIEW_DELETED",
                report.getReviewId()
            );
        }

        // Notify reporter (báo cáo được chấp nhận)
        notifService.send(
            report.getReporterId(),
            "Báo cáo đã được xử lý",
            "Cảm ơn bạn đã báo cáo! Đánh giá vi phạm đã được xoá.",
            "REPORT_APPROVED",
            reportId
        );
    }

    /** Admin: từ chối xoá review */
    public void reject(Long reportId, Long adminId, String note) {
        ReviewReportDTOs.ReportListDTO report = repo.findById(reportId);
        if (report == null) throw new IllegalStateException("Report không tồn tại");
        if (note == null || note.isBlank()) throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");

        repo.reject(reportId, adminId, note);

        // Chỉ notify reporter
        notifService.send(
            report.getReporterId(),
            "Báo cáo của bạn đã bị từ chối",
            "Admin đã xem xét và từ chối báo cáo của bạn. Lý do: " + note,
            "REPORT_REJECTED",
            reportId
        );
    }
}
