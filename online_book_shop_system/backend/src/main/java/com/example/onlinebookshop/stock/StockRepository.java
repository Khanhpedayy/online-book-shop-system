package com.example.onlinebookshop.stock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockRepository {

    private final JdbcTemplate jdbc;

    public StockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ═══ LIST STOCK ═══ */

    public List<StockItemDTO> findAllStock() {
        String sql = "SELECT b.id AS book_id, b.title, b.isbn13, c.name AS category_name, "
                + "b.status, ISNULL(b.stock_quantity, 0) AS stock_quantity, "
                + "(SELECT TOP 1 bi.url FROM book_images bi "
                + " WHERE bi.book_id = b.id AND bi.is_cover = 1 AND bi.deleted_at IS NULL "
                + " ORDER BY bi.sort_order) AS cover_image_url "
                + "FROM books b "
                + "LEFT JOIN categories c ON c.id = b.category_id "
                + "WHERE b.deleted_at IS NULL "
                + "ORDER BY b.title";
        return jdbc.query(sql, (rs, i) -> {
            StockItemDTO d = new StockItemDTO();
            d.setBookId(rs.getLong("book_id"));
            d.setTitle(rs.getString("title"));
            d.setIsbn13(rs.getString("isbn13"));
            d.setCategoryName(rs.getString("category_name"));
            d.setStatus(rs.getString("status"));
            d.setStockQuantity(rs.getInt("stock_quantity"));
            d.setCoverImageUrl(rs.getString("cover_image_url"));
            return d;
        });
    }

    /* ═══ GET STOCK FOR ONE BOOK ═══ */

    public Integer getStockQuantity(Long bookId) {
        return jdbc.queryForObject(
                "SELECT ISNULL(stock_quantity, 0) FROM books WHERE id = ? AND deleted_at IS NULL",
                Integer.class, bookId);
    }

    /* ═══ UPDATE STOCK QUANTITY ═══ */

    public void setStockQuantity(Long bookId, int quantity) {
        jdbc.update("UPDATE books SET stock_quantity = ?, updated_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL",
                quantity, bookId);
    }

    /* ═══ INSERT ADJUSTMENT RECORD ═══ */

    public void insertAdjustment(Long bookId, String type, int quantity, int oldQty, int newQty,
                                  String reason, String note) {
        jdbc.update("INSERT INTO stock_adjustments (book_id, adjustment_type, quantity, old_quantity, "
                        + "new_quantity, reason, note) VALUES (?, ?, ?, ?, ?, ?, ?)",
                bookId, type, quantity, oldQty, newQty, reason, note);
    }

    /* ═══ GET ADJUSTMENTS ═══ */

    public List<StockAdjustmentDTO> findAdjustments(Long bookId) {
        String sql = "SELECT sa.id, sa.book_id, b.title AS book_title, sa.adjustment_type, "
                + "sa.quantity, sa.old_quantity, sa.new_quantity, sa.reason, sa.note, sa.created_at "
                + "FROM stock_adjustments sa "
                + "JOIN books b ON b.id = sa.book_id "
                + "WHERE " + (bookId != null ? "sa.book_id = ? " : "1=1 ")
                + "ORDER BY sa.created_at DESC";
        if (bookId != null) {
            return jdbc.query(sql, (rs, i) -> mapAdjustment(rs), bookId);
        }
        return jdbc.query(sql, (rs, i) -> mapAdjustment(rs));
    }

    /* ═══ CHECK BOOK EXISTS ═══ */

    public boolean bookExists(Long bookId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM books WHERE id = ? AND deleted_at IS NULL", Integer.class, bookId);
        return count != null && count > 0;
    }

    private StockAdjustmentDTO mapAdjustment(java.sql.ResultSet rs) throws java.sql.SQLException {
        StockAdjustmentDTO d = new StockAdjustmentDTO();
        d.setId(rs.getLong("id"));
        d.setBookId(rs.getLong("book_id"));
        d.setBookTitle(rs.getString("book_title"));
        d.setAdjustmentType(rs.getString("adjustment_type"));
        d.setQuantity(rs.getInt("quantity"));
        d.setOldQuantity(rs.getInt("old_quantity"));
        d.setNewQuantity(rs.getInt("new_quantity"));
        d.setReason(rs.getString("reason"));
        d.setNote(rs.getString("note"));
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        return d;
    }
}
