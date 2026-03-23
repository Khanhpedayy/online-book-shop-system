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
        Integer val = jdbc.queryForObject(
                "SELECT ISNULL(SUM(stock_quantity), 0) FROM books WHERE deleted_at IS NULL", Integer.class);
        return val != null ? val : 0;
    }

    public int countOutOfStock() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM books WHERE deleted_at IS NULL AND ISNULL(stock_quantity, 0) = 0", Integer.class);
    }

    public int countLowStock() {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM books WHERE deleted_at IS NULL AND stock_quantity > 0 AND stock_quantity <= 5",
                Integer.class);
    }

    /* ═══ RECENT BOOKS ═══ */

    public List<RecentBookDTO> findRecentBooks(int limit) {
        String sql = "SELECT TOP " + limit
                + " b.id, b.title, c.name AS category_name, b.status, "
                + "ISNULL(b.stock_quantity, 0) AS stock_quantity, b.created_at "
                + "FROM books b "
                + "LEFT JOIN categories c ON c.id = b.category_id "
                + "WHERE b.deleted_at IS NULL "
                + "ORDER BY b.created_at DESC";
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
        String sql = "SELECT b.id AS book_id, b.title, ISNULL(b.stock_quantity, 0) AS stock_quantity, "
                + "CASE WHEN ISNULL(b.stock_quantity, 0) = 0 THEN 'OUT_OF_STOCK' ELSE 'LOW_STOCK' END AS alert_type "
                + "FROM books b "
                + "WHERE b.deleted_at IS NULL AND ISNULL(b.stock_quantity, 0) <= 5 "
                + "ORDER BY b.stock_quantity ASC, b.title";
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
        String sql = "SELECT ISNULL(c.name, N'Chưa phân loại') AS category_name, "
                + "COUNT(b.id) AS book_count, "
                + "ISNULL(SUM(b.stock_quantity), 0) AS total_stock "
                + "FROM books b "
                + "LEFT JOIN categories c ON c.id = b.category_id AND c.deleted_at IS NULL "
                + "WHERE b.deleted_at IS NULL "
                + "GROUP BY c.name "
                + "ORDER BY book_count DESC";
        return jdbc.query(sql, (rs, i) -> {
            CategoryStatsDTO d = new CategoryStatsDTO();
            d.setCategoryName(rs.getString("category_name"));
            d.setBookCount(rs.getInt("book_count"));
            d.setTotalStock(rs.getInt("total_stock"));
            return d;
        });
    }
}
