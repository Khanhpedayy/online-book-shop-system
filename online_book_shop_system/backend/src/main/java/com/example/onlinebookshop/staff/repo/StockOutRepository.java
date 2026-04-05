package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
public class StockOutRepository {

    private final JdbcTemplate jdbc;

    public StockOutRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsActive(Long orderId) {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.stock_outs
                WHERE order_id = ?
                  AND deleted_at IS NULL
                  AND status <> 'CANCELLED'
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }

    public Long insert(Long orderId, Long userId) {
        String code = "SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        String sql = """
                INSERT INTO dbo.stock_outs
                (stock_out_code, order_id, status, has_exception, created_at, created_by)
                OUTPUT INSERTED.id
                VALUES (?, ?, 'CREATED', 0, GETDATE(), ?)
                """;

        return jdbc.queryForObject(sql, Long.class, code, orderId, userId);
    }

    public Optional<StockOutSummary> findActiveSummaryByOrderId(Long orderId) {
        String sql = """
                SELECT TOP 1
                       id,
                       stock_out_code,
                       order_id,
                       status,
                       printed_at,
                       has_exception,
                       exception_note
                FROM dbo.stock_outs
                WHERE order_id = ?
                  AND deleted_at IS NULL
                  AND status <> 'CANCELLED'
                ORDER BY id DESC
                """;

        List<StockOutSummary> rows = jdbc.query(sql, (rs, rowNum) -> {
            Timestamp printedAt = rs.getTimestamp("printed_at");
            return new StockOutSummary(
                    rs.getLong("id"),
                    rs.getString("stock_out_code"),
                    rs.getLong("order_id"),
                    rs.getString("status"),
                    printedAt == null ? null : printedAt.toLocalDateTime(),
                    rs.getBoolean("has_exception"),
                    rs.getString("exception_note")
            );
        }, orderId);

        return rows.stream().findFirst();
    }

    public Optional<StockOutSummary> findSummaryById(Long stockOutId) {
        String sql = """
                SELECT
                    id,
                    stock_out_code,
                    order_id,
                    status,
                    printed_at,
                    has_exception,
                    exception_note
                FROM dbo.stock_outs
                WHERE id = ?
                  AND deleted_at IS NULL
                """;

        List<StockOutSummary> rows = jdbc.query(sql, (rs, rowNum) -> {
            Timestamp printedAt = rs.getTimestamp("printed_at");
            return new StockOutSummary(
                    rs.getLong("id"),
                    rs.getString("stock_out_code"),
                    rs.getLong("order_id"),
                    rs.getString("status"),
                    printedAt == null ? null : printedAt.toLocalDateTime(),
                    rs.getBoolean("has_exception"),
                    rs.getString("exception_note")
            );
        }, stockOutId);

        return rows.stream().findFirst();
    }

    public void markPrintedIfCreated(Long stockOutId) {
        String sql = """
                UPDATE dbo.stock_outs
                SET status = 'PRINTED',
                    printed_at = COALESCE(printed_at, GETDATE()),
                    updated_at = GETDATE()
                WHERE id = ?
                  AND deleted_at IS NULL
                  AND status = 'CREATED'
                """;

        jdbc.update(sql, stockOutId);
    }

    public void markPickingIfNeeded(Long stockOutId) {
        String sql = """
                UPDATE dbo.stock_outs
                SET status = 'PICKING',
                    printed_at = COALESCE(printed_at, GETDATE()),
                    updated_at = GETDATE()
                WHERE id = ?
                  AND deleted_at IS NULL
                  AND status IN ('CREATED', 'PRINTED')
                """;

        jdbc.update(sql, stockOutId);
    }

    public void markException(Long stockOutId, String note) {
        String sql = """
                UPDATE dbo.stock_outs
                SET has_exception = 1,
                    exception_note = ?,
                    status = CASE
                                WHEN status IN ('CREATED', 'PRINTED') THEN 'PICKING'
                                ELSE status
                             END,
                    updated_at = GETDATE()
                WHERE id = ?
                  AND deleted_at IS NULL
                """;

        jdbc.update(sql, note, stockOutId);
    }

    public void markPickedIfReady(Long stockOutId, Long userId) {
        String sql = """
                UPDATE so
                SET so.status = 'PICKED',
                    so.picked_at = COALESCE(so.picked_at, GETDATE()),
                    so.picked_by = COALESCE(so.picked_by, ?),
                    so.updated_at = GETDATE()
                FROM dbo.stock_outs so
                WHERE so.id = ?
                  AND so.deleted_at IS NULL
                  AND so.has_exception = 0
                  AND NOT EXISTS (
                      SELECT 1
                      FROM dbo.stock_out_items soi
                      WHERE soi.stock_out_id = so.id
                        AND soi.deleted_at IS NULL
                        AND (
                            soi.picked_at IS NULL
                            OR soi.is_missing_reported = 1
                        )
                  )
                """;

        jdbc.update(sql, userId, stockOutId);
    }

    public static class StockOutSummary {
        private final Long id;
        private final String stockOutCode;
        private final Long orderId;
        private final String status;
        private final LocalDateTime printedAt;
        private final boolean hasException;
        private final String exceptionNote;

        public StockOutSummary(Long id,
                               String stockOutCode,
                               Long orderId,
                               String status,
                               LocalDateTime printedAt,
                               boolean hasException,
                               String exceptionNote) {
            this.id = id;
            this.stockOutCode = stockOutCode;
            this.orderId = orderId;
            this.status = status;
            this.printedAt = printedAt;
            this.hasException = hasException;
            this.exceptionNote = exceptionNote;
        }

        public Long getId() {
            return id;
        }

        public String getStockOutCode() {
            return stockOutCode;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getPrintedAt() {
            return printedAt;
        }

        public boolean isHasException() {
            return hasException;
        }

        public String getExceptionNote() {
            return exceptionNote;
        }
    }
}