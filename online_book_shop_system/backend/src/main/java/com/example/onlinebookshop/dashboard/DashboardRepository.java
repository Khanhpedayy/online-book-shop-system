package com.example.onlinebookshop.dashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DashboardRepository {

    private final JdbcTemplate jdbc;

    public DashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /*
     * OUTER APPLY subquery để tính tồn kho thực từ copies.
     * Dùng OUTER APPLY thay vì correlated subquery trong SUM() vì SQL Server
     * không cho phép SUM(subquery) trực tiếp.
     * Alias bảng ngoài phải là 'b' (books).
     */
    private static final String STOCK_APPLY =
            "OUTER APPLY (SELECT ISNULL(COUNT(*), 0) AS stock_qty FROM copies cp " +
            " JOIN book_variants bv ON bv.id = cp.variant_id " +
            " WHERE bv.book_id = b.id AND cp.status = 'AVAILABLE' AND cp.deleted_at IS NULL) s ";

    /* ═══ BOOK COUNTS ═══ */

    public int countBooks() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM books WHERE deleted_at IS NULL", Integer.class);
    }

    public int countBooksByStatus(String status) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM books WHERE status = ? AND deleted_at IS NULL", Integer.class, status);
    }

    /* ═══ CATEGORY COUNTS ═══ */

    public int countCategories() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE deleted_at IS NULL", Integer.class);
    }

    public int countActiveCategories() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM categories WHERE is_active = 1 AND deleted_at IS NULL", Integer.class);
    }

    /* ═══ STOCK AGGREGATES ═══ */

    public int sumStockQuantity() {
        // Tổng tồn kho = count copies AVAILABLE trên toàn hệ thống
        Integer val = jdbc.queryForObject(
                "SELECT ISNULL(COUNT(*), 0) FROM copies c " +
                "JOIN book_variants v ON v.id = c.variant_id " +
                "JOIN books b ON b.id = v.book_id " +
                "WHERE c.status = 'AVAILABLE' AND c.deleted_at IS NULL AND b.deleted_at IS NULL",
                Integer.class);
        return val != null ? val : 0;
    }

    public int countOutOfStock() {
        // Sách hết hàng = không có copy AVAILABLE nào
        Integer val = jdbc.queryForObject(
                "SELECT COUNT(*) FROM books b " + STOCK_APPLY +
                "WHERE b.deleted_at IS NULL AND s.stock_qty = 0",
                Integer.class);
        return val != null ? val : 0;
    }

    public int countLowStock() {
        // Sắp hết = 1–5 copies AVAILABLE
        Integer val = jdbc.queryForObject(
                "SELECT COUNT(*) FROM books b " + STOCK_APPLY +
                "WHERE b.deleted_at IS NULL AND s.stock_qty > 0 AND s.stock_qty <= 5",
                Integer.class);
        return val != null ? val : 0;
    }

    /* ═══ RECENT BOOKS ═══ */

    public List<RecentBookDTO> findRecentBooks(int limit) {
        String sql = "SELECT TOP " + limit
                + " b.id, b.title, c.name AS category_name, b.status,"
                + " s.stock_qty AS stock_quantity, b.created_at"
                + " FROM books b"
                + " LEFT JOIN categories c ON c.id = b.category_id"
                + " " + STOCK_APPLY
                + " WHERE b.deleted_at IS NULL"
                + " ORDER BY b.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            RecentBookDTO d = new RecentBookDTO();
            d.setId(rs.getLong("id"));
            d.setTitle(rs.getString("title"));
            d.setCategoryName(rs.getString("category_name"));
            d.setStatus(rs.getString("status"));
            d.setStockQuantity(rs.getInt("stock_quantity"));
            d.setCreatedAt(rs.getString("created_at"));
            return d;
        });
    }

    /* ═══ STOCK ALERTS ═══ */

    public List<StockAlertDTO> findStockAlerts() {
        // Dùng OUTER APPLY để ORDER BY s.stock_qty hợp lệ
        String sql = "SELECT b.id AS book_id, b.title,"
                + " s.stock_qty AS stock_quantity,"
                + " CASE WHEN s.stock_qty = 0 THEN 'OUT_OF_STOCK' ELSE 'LOW_STOCK' END AS alert_type"
                + " FROM books b"
                + " " + STOCK_APPLY
                + " WHERE b.deleted_at IS NULL AND s.stock_qty <= 5"
                + " ORDER BY s.stock_qty ASC, b.title";
        return jdbc.query(sql, (rs, i) -> {
            StockAlertDTO d = new StockAlertDTO();
            d.setBookId(rs.getLong("book_id"));
            d.setTitle(rs.getString("title"));
            d.setStockQuantity(rs.getInt("stock_quantity"));
            d.setAlertType(rs.getString("alert_type"));
            return d;
        });
    }

    /* ═══ CATEGORY DISTRIBUTION ═══ */

    public List<CategoryStatsDTO> getCategoryStats() {
        // OUTER APPLY cho phép SUM(s.stock_qty) hợp lệ trong SQL Server
        String sql = "SELECT ISNULL(c.name, N'Chưa phân loại') AS category_name,"
                + " COUNT(b.id) AS book_count,"
                + " ISNULL(SUM(s.stock_qty), 0) AS total_stock"
                + " FROM books b"
                + " LEFT JOIN categories c ON c.id = b.category_id AND c.deleted_at IS NULL"
                + " " + STOCK_APPLY
                + " WHERE b.deleted_at IS NULL"
                + " GROUP BY c.name"
                + " ORDER BY book_count DESC";
        return jdbc.query(sql, (rs, i) -> {
            CategoryStatsDTO d = new CategoryStatsDTO();
            d.setCategoryName(rs.getString("category_name"));
            d.setBookCount(rs.getInt("book_count"));
            d.setTotalStock(rs.getInt("total_stock"));
            return d;
        });
    }
}
