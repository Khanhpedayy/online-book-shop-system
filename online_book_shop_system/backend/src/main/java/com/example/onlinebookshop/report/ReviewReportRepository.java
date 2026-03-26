package com.example.onlinebookshop.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewReportRepository {

    private final JdbcTemplate jdbc;

    public ReviewReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Tạo report mới + đổi status review → REPORTED */
    public void create(Long reporterId, ReviewReportDTOs.CreateReportRequest req) {
        jdbc.update(
            "INSERT INTO review_reports (review_id, reporter_id, reason, status, created_at) " +
            "VALUES (?, ?, ?, 'PENDING', SYSUTCDATETIME())",
            req.getReviewId(), reporterId, req.getReason()
        );
        jdbc.update(
            "UPDATE reviews SET status='REPORTED' WHERE id=? AND deleted_at IS NULL",
            req.getReviewId()
        );
    }

    /** User đã report review này chưa? */
    public boolean hasReported(Long reporterId, Long reviewId) {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM review_reports WHERE reporter_id=? AND review_id=? AND deleted_at IS NULL",
            Integer.class, reporterId, reviewId
        );
        return c != null && c > 0;
    }

    /** Kiểm tra review có tồn tại và user có phải người viết không */
    public Long getReviewAuthorId(Long reviewId) {
        List<Long> rows = jdbc.query(
            "SELECT user_id FROM reviews WHERE id=? AND deleted_at IS NULL",
            (rs, i) -> rs.getLong("user_id"), reviewId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Admin: lấy tất cả reports */
    public List<ReviewReportDTOs.ReportListDTO> findAll(String statusFilter) {
        String where = (statusFilter != null && !statusFilter.isBlank())
                       ? "WHERE rr.status = '" + statusFilter.replace("'", "") + "' AND rr.deleted_at IS NULL"
                       : "WHERE rr.deleted_at IS NULL";
        String sql =
            "SELECT rr.id, rr.review_id, r.content AS review_content, r.title AS review_title, r.rating AS review_rating, " +
            "  ur.full_name AS reviewer_name, " +
            "  rep.full_name AS reporter_name, rep.id AS reporter_id, " +
            "  rr.reason, rr.status, rr.admin_note, rr.created_at " +
            "FROM review_reports rr " +
            "JOIN reviews r   ON rr.review_id   = r.id " +
            "JOIN users   ur  ON r.user_id       = ur.id " +
            "JOIN users   rep ON rr.reporter_id  = rep.id " +
            where + " ORDER BY rr.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            ReviewReportDTOs.ReportListDTO d = new ReviewReportDTOs.ReportListDTO();
            d.setId(rs.getLong("id"));
            d.setReviewId(rs.getLong("review_id"));
            d.setReviewContent(rs.getString("review_content"));
            d.setReviewTitle(rs.getString("review_title"));
            d.setReviewRating(rs.getInt("review_rating"));
            d.setReviewerName(rs.getString("reviewer_name"));
            d.setReporterName(rs.getString("reporter_name"));
            d.setReporterId(rs.getLong("reporter_id"));
            d.setReason(rs.getString("reason"));
            d.setStatus(rs.getString("status"));
            d.setAdminNote(rs.getString("admin_note"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        });
    }

    /** Lấy 1 report theo ID */
    public ReviewReportDTOs.ReportListDTO findById(Long id) {
        List<ReviewReportDTOs.ReportListDTO> list = findAll(null);
        return list.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    /** Admin approve → soft-delete review + cập nhật report */
    public void approve(Long reportId, Long adminId) {
        // Lấy review_id
        Long reviewId = jdbc.queryForObject(
            "SELECT review_id FROM review_reports WHERE id=?", Long.class, reportId
        );
        // Xoá mềm review
        jdbc.update(
            "UPDATE reviews SET deleted_at=SYSUTCDATETIME(), status='HIDDEN' WHERE id=?",
            reviewId
        );
        // Cập nhật report
        jdbc.update(
            "UPDATE review_reports SET status='APPROVED', reviewed_by=?, reviewed_at=SYSUTCDATETIME(), updated_at=SYSUTCDATETIME() WHERE id=?",
            adminId, reportId
        );
    }

    /** Admin reject → cập nhật report + admin_note */
    public void reject(Long reportId, Long adminId, String note) {
        // Trả review về PUBLISHED
        Long reviewId = jdbc.queryForObject(
            "SELECT review_id FROM review_reports WHERE id=?", Long.class, reportId
        );
        jdbc.update(
            "UPDATE reviews SET status='PUBLISHED' WHERE id=? AND deleted_at IS NULL",
            reviewId
        );
        jdbc.update(
            "UPDATE review_reports SET status='REJECTED', admin_note=?, reviewed_by=?, reviewed_at=SYSUTCDATETIME(), updated_at=SYSUTCDATETIME() WHERE id=?",
            note, adminId, reportId
        );
    }
}
