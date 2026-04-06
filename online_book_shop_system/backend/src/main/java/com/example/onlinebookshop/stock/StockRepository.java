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

    /* ══ Subquery đếm có sẵn từ copies theo book (AVAILABLE) ══ */
    private static final String COPY_AVAILABLE_SUBQUERY =
            "(SELECT ISNULL(COUNT(*), 0) " +
            " FROM copies c " +
            " JOIN book_variants v ON v.id = c.variant_id " +
            " WHERE v.book_id = b.id AND c.status = 'AVAILABLE' AND c.deleted_at IS NULL)";

    /* ══ Subquery đếm hư hỏng/mất từ copies theo book ══ */
    private static final String COPY_DAMAGED_SUBQUERY =
            "(SELECT ISNULL(COUNT(*), 0) " +
            " FROM copies c " +
            " JOIN book_variants v ON v.id = c.variant_id " +
            " WHERE v.book_id = b.id AND c.status IN ('DAMAGED','LOST') AND c.deleted_at IS NULL)";

    /* ══ Subquery tổng SL đã nhận từ lots theo book (lịch sử) ══ */
    private static final String LOT_RECEIVED_SUBQUERY =
            "(SELECT ISNULL(SUM(l.qty_received), 0) " +
            " FROM lots l " +
            " JOIN book_variants v ON v.id = l.variant_id " +
            " WHERE v.book_id = b.id AND l.deleted_at IS NULL)";

    /* ══ LIST STOCK (tính từ copies table - chính xác thực tế) ══ */
    public List<StockItemDTO> findAllStock() {
        String sql = "SELECT b.id AS book_id, b.title, b.isbn13, c.name AS category_name, b.status, "
                + COPY_AVAILABLE_SUBQUERY + " AS stock_quantity, "
                + COPY_DAMAGED_SUBQUERY + " AS damaged_quantity, "
                + LOT_RECEIVED_SUBQUERY + " AS received_quantity, "
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
            d.setDamagedQuantity(rs.getInt("damaged_quantity"));
            d.setReceivedQuantity(rs.getInt("received_quantity"));
            d.setCoverImageUrl(rs.getString("cover_image_url"));
            return d;
        });
    }

    /* ═══ GET STOCK FOR ONE BOOK (aggregate từ lots) ═══ */
    public int getStockQuantity(Long bookId) {
        Integer val = jdbc.queryForObject(
                "SELECT ISNULL(SUM(l.qty_available), 0) " +
                "FROM lots l " +
                "JOIN book_variants v ON v.id = l.variant_id " +
                "WHERE v.book_id = ? AND l.deleted_at IS NULL",
                Integer.class, bookId);
        return val != null ? val : 0;
    }

    /**
     * Decrements {@code lots.qty_available} for the given book (FIFO by lot id).
     *
     * @return units actually deducted (equals {@code requestedQty} when enough stock existed)
     */
    public int decrementAvailableForBook(long bookId, int requestedQty) {
        if (requestedQty <= 0) {
            return 0;
        }
        int remaining = requestedQty;
        while (remaining > 0) {
            Long lotId = jdbc.query(
                    """
                            SELECT TOP (1) l.id
                            FROM lots l
                            INNER JOIN book_variants v ON v.id = l.variant_id
                            WHERE v.book_id = ? AND l.deleted_at IS NULL AND l.qty_available > 0
                            ORDER BY l.id
                            """,
                    ps -> ps.setLong(1, bookId),
                    rs -> rs.next() ? rs.getLong(1) : null);
            if (lotId == null) {
                break;
            }
            Integer available = jdbc.queryForObject(
                    "SELECT qty_available FROM lots WHERE id = ? AND deleted_at IS NULL",
                    Integer.class,
                    lotId);
            int av = available != null ? available : 0;
            if (av <= 0) {
                continue;
            }
            int take = Math.min(remaining, av);
            int updated = jdbc.update(
                    """
                            UPDATE lots
                            SET qty_available = qty_available - ?,
                                updated_at = SYSUTCDATETIME()
                            WHERE id = ?
                              AND deleted_at IS NULL
                              AND qty_available >= ?
                            """,
                    take,
                    lotId,
                    take);
            if (updated != 1) {
                throw new IllegalStateException("Concurrent inventory update for lot id=" + lotId);
            }
            remaining -= take;
        }
        return requestedQty - remaining;
    }

    /** Sets {@code books.stock_quantity} to the current sum of {@code lots.qty_available} for the book. */
    public void refreshBookStockQuantityFromLots(long bookId) {
        jdbc.update(
                """
                        UPDATE b
                        SET b.stock_quantity = s.total,
                            b.updated_at = SYSUTCDATETIME()
                        FROM books b
                        CROSS APPLY (
                            SELECT ISNULL(SUM(l.qty_available), 0) AS total
                            FROM lots l
                            INNER JOIN book_variants v ON v.id = l.variant_id
                            WHERE v.book_id = b.id AND l.deleted_at IS NULL
                        ) s
                        WHERE b.id = ?
                          AND b.deleted_at IS NULL
                        """,
                bookId);
    }

    /* ═══ GET ADJUSTMENTS (lịch sử inventory_transactions) ═══ */
    public List<StockAdjustmentDTO> findAdjustments(Long bookId) {
        // Đọc từ inventory_transactions thay vì stock_adjustments, lọc theo book nếu có
        String sql = "SELECT it.id, v.book_id, b.title AS book_title, it.movement_type AS adjustment_type, "
                + "it.quantity, it.reason, it.note, it.created_at "
                + "FROM inventory_transactions it "
                + "JOIN book_variants v ON v.id = it.variant_id "
                + "JOIN books b ON b.id = v.book_id "
                + "WHERE it.reference_type = 'STOCKTAKING' "
                + (bookId != null ? "AND v.book_id = ? " : "")
                + "ORDER BY it.created_at DESC";
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
        d.setReason(rs.getString("reason"));
        d.setNote(rs.getString("note"));
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        return d;
    }
}
