package com.example.onlinebookshop.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbc;

    public ReviewRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lấy danh sách sách đã mua (đơn DELIVERED hoặc COMPLETED) của user,
     * kèm trạng thái đã review chưa.
     */
    public List<ReviewDTOs.PurchasedBookDTO> findPurchasedBooks(Long userId) {
        String sql =
            "SELECT DISTINCT " +
            "  b.id AS book_id, b.title AS book_title, " +
            "  (SELECT TOP 1 bi.url FROM book_images bi WHERE bi.book_id = b.id AND bi.is_cover = 1) AS cover_url, " +
            "  bv.id AS variant_id, bv.sku, " +
            "  o.id AS order_id, o.order_code, " +
            "  r.id AS review_id, r.rating AS review_rating, " +
            "  CASE WHEN r.id IS NOT NULL THEN 1 ELSE 0 END AS reviewed " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN book_variants bv ON oi.variant_id = bv.id " +
            "JOIN books b ON bv.book_id = b.id " +
            "LEFT JOIN reviews r ON r.book_id = b.id AND r.user_id = ? AND r.deleted_at IS NULL " +
            "WHERE o.user_id = ? AND o.status IN ('DELIVERED','COMPLETED') " +
            "ORDER BY reviewed ASC, b.title ASC";

        return jdbc.query(sql, (rs, i) -> {
            ReviewDTOs.PurchasedBookDTO d = new ReviewDTOs.PurchasedBookDTO();
            d.setBookId(rs.getLong("book_id"));
            d.setBookTitle(rs.getString("book_title"));
            d.setBookCoverUrl(rs.getString("cover_url"));
            d.setVariantId(rs.getLong("variant_id"));
            d.setSku(rs.getString("sku"));
            d.setOrderId(rs.getLong("order_id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setReviewed(rs.getInt("reviewed") == 1);
            if (rs.getObject("review_id") != null) d.setReviewId(rs.getLong("review_id"));
            if (rs.getObject("review_rating") != null) d.setReviewRating(rs.getInt("review_rating"));
            return d;
        }, userId, userId);
    }

    /** Tạo review mới */
    public Long createReview(Long userId, ReviewDTOs.CreateReviewRequest req) {
        jdbc.update(
            "INSERT INTO reviews (user_id, book_id, order_id, rating, title, content, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, 'PUBLISHED', SYSUTCDATETIME())",
            userId, req.getBookId(), req.getOrderId(), req.getRating(), req.getTitle(), req.getContent()
        );
        return jdbc.queryForObject(
            "SELECT TOP 1 id FROM reviews WHERE user_id=? AND book_id=? AND deleted_at IS NULL ORDER BY created_at DESC",
            Long.class, userId, req.getBookId()
        );
    }

    /** Update review */
    public void updateReview(Long reviewId, Long userId, ReviewDTOs.CreateReviewRequest req) {
        jdbc.update(
            "UPDATE reviews SET rating=?, title=?, content=?, updated_at=SYSUTCDATETIME() " +
            "WHERE id=? AND user_id=? AND deleted_at IS NULL",
            req.getRating(), req.getTitle(), req.getContent(), reviewId, userId
        );
    }

    /** Xóa mềm review */
    public void deleteReview(Long reviewId, Long userId) {
        jdbc.update(
            "UPDATE reviews SET deleted_at=SYSUTCDATETIME() WHERE id=? AND user_id=? AND deleted_at IS NULL",
            reviewId, userId
        );
    }

    /** Lấy tất cả review PUBLISHED của 1 cuốn sách */
    public List<ReviewDTOs.ReviewResponse> findPublishedByBookId(Long bookId) {
        String sql =
            "SELECT r.id, r.book_id, b.title AS book_title, r.order_id, r.rating, r.title, r.content, r.status, " +
            "  r.created_at, u.full_name AS reviewer_name " +
            "FROM reviews r " +
            "JOIN books b ON r.book_id = b.id " +
            "JOIN users u ON r.user_id = u.id " +
            "WHERE r.book_id=? AND r.status='PUBLISHED' AND r.deleted_at IS NULL " +
            "ORDER BY r.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            ReviewDTOs.ReviewResponse d = new ReviewDTOs.ReviewResponse();
            d.setId(rs.getLong("id"));
            d.setBookId(rs.getLong("book_id"));
            d.setBookTitle(rs.getString("book_title"));
            d.setOrderId(rs.getObject("order_id") != null ? rs.getLong("order_id") : null);
            d.setRating(rs.getInt("rating"));
            d.setTitle(rs.getString("title"));
            d.setContent(rs.getString("content"));
            d.setStatus(rs.getString("status"));
            d.setReviewerName(rs.getString("reviewer_name"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, bookId);
    }

    /** Lấy review của user cho 1 cuốn sách */
    public ReviewDTOs.ReviewResponse findByUserAndBook(Long userId, Long bookId) {
        String sql =
            "SELECT r.id, r.book_id, b.title AS book_title, r.order_id, r.rating, r.title, r.content, r.status, r.created_at " +
            "FROM reviews r JOIN books b ON r.book_id = b.id " +
            "WHERE r.user_id=? AND r.book_id=? AND r.deleted_at IS NULL";
        List<ReviewDTOs.ReviewResponse> list = jdbc.query(sql, (rs, i) -> {
            ReviewDTOs.ReviewResponse d = new ReviewDTOs.ReviewResponse();
            d.setId(rs.getLong("id"));
            d.setBookId(rs.getLong("book_id"));
            d.setBookTitle(rs.getString("book_title"));
            d.setOrderId(rs.getObject("order_id") != null ? rs.getLong("order_id") : null);
            d.setRating(rs.getInt("rating"));
            d.setTitle(rs.getString("title"));
            d.setContent(rs.getString("content"));
            d.setStatus(rs.getString("status"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, userId, bookId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Kiểm tra đã review cuốn này chưa */
    public boolean hasReviewed(Long userId, Long bookId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM reviews WHERE user_id=? AND book_id=? AND deleted_at IS NULL",
            Integer.class, userId, bookId
        );
        return count != null && count > 0;
    }

    /** Kiểm tra đã mua sách này chưa (đơn DELIVERED hoặc COMPLETED) */
    public boolean hasPurchased(Long userId, Long bookId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN book_variants bv ON oi.variant_id = bv.id " +
            "WHERE o.user_id=? AND bv.book_id=? AND o.status IN ('DELIVERED','COMPLETED')",
            Integer.class, userId, bookId
        );
        return count != null && count > 0;
    }
}
