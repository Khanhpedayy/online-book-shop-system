package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StockOutItemRepository {

    private final JdbcTemplate jdbc;

    public StockOutItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countUnallocatedItems(Long orderId) {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.order_items
                WHERE order_id = ?
                  AND deleted_at IS NULL
                  AND copy_id IS NULL
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, orderId);
        return count == null ? 0 : count;
    }

    public void insertFromOrder(Long orderId, Long stockOutId) {
        String sql = """
                INSERT INTO dbo.stock_out_items (
                    stock_out_id,
                    order_item_id,
                    variant_id,
                    copy_id,
                    lot_id,
                    title_snapshot,
                    sku_snapshot,
                    copy_code_snapshot,
                    location_snapshot,
                    quantity,
                    created_at
                )
                SELECT
                    ?,
                    oi.id,
                    oi.variant_id,
                    oi.copy_id,
                    c.lot_id,
                    oi.title_snapshot,
                    oi.sku_snapshot,
                    c.copy_code,
                    c.location,
                    oi.quantity,
                    GETDATE()
                FROM dbo.order_items oi
                JOIN dbo.copies c ON c.id = oi.copy_id
                WHERE oi.order_id = ?
                  AND oi.deleted_at IS NULL
                """;

        jdbc.update(sql, stockOutId, orderId);
    }

    public long countItemsByStockOutId(Long stockOutId) {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.stock_out_items
                WHERE stock_out_id = ?
                  AND deleted_at IS NULL
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, stockOutId);
        return count == null ? 0 : count;
    }

    public List<PickItemRow> findPickRows(Long stockOutId) {
        String sql = """
                SELECT
                    soi.id,
                    soi.order_item_id,
                    soi.title_snapshot,
                    soi.sku_snapshot,
                    soi.copy_code_snapshot,
                    soi.location_snapshot,
                    soi.quantity,
                    soi.picked_at,
                    soi.is_missing_reported,
                    soi.missing_note
                FROM dbo.stock_out_items soi
                WHERE soi.stock_out_id = ?
                  AND soi.deleted_at IS NULL
                ORDER BY COALESCE(soi.location_snapshot, 'ZZZ'),
                         soi.title_snapshot,
                         soi.id
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Timestamp pickedAt = rs.getTimestamp("picked_at");
            return new PickItemRow(
                    rs.getLong("id"),
                    rs.getLong("order_item_id"),
                    rs.getString("title_snapshot"),
                    rs.getString("sku_snapshot"),
                    rs.getString("copy_code_snapshot"),
                    rs.getString("location_snapshot"),
                    rs.getInt("quantity"),
                    pickedAt == null ? null : pickedAt.toLocalDateTime(),
                    rs.getBoolean("is_missing_reported"),
                    rs.getString("missing_note")
            );
        }, stockOutId);
    }

    public boolean itemBelongsToStockOut(Long stockOutId, Long itemId) {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.stock_out_items
                WHERE id = ?
                  AND stock_out_id = ?
                  AND deleted_at IS NULL
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, itemId, stockOutId);
        return count != null && count > 0;
    }

    public void markPicked(Long itemId, Long userId) {
        String sql = """
                UPDATE dbo.stock_out_items
                SET picked_by = ?,
                    picked_at = COALESCE(picked_at, GETDATE()),
                    updated_at = GETDATE()
                WHERE id = ?
                  AND deleted_at IS NULL
                  AND is_missing_reported = 0
                """;

        jdbc.update(sql, userId, itemId);
    }

    public void updateCopyStatusPicked(Long itemId) {
        String sql = """
                UPDATE c
                SET c.status = 'PICKED',
                    c.updated_at = GETDATE()
                FROM dbo.copies c
                JOIN dbo.stock_out_items soi ON soi.copy_id = c.id
                WHERE soi.id = ?
                  AND c.deleted_at IS NULL
                """;

        jdbc.update(sql, itemId);
    }

    public void updateOrderItemPicked(Long itemId, Long userId) {
        String sql = """
                UPDATE oi
                SET oi.picked_by = ?,
                    oi.picked_at = COALESCE(oi.picked_at, GETDATE()),
                    oi.updated_at = GETDATE()
                FROM dbo.order_items oi
                JOIN dbo.stock_out_items soi ON soi.order_item_id = oi.id
                WHERE soi.id = ?
                  AND oi.deleted_at IS NULL
                """;

        jdbc.update(sql, userId, itemId);
    }

    public void markMissing(Long itemId, String note) {
        String sql = """
                UPDATE dbo.stock_out_items
                SET is_missing_reported = 1,
                    missing_note = ?,
                    updated_at = GETDATE()
                WHERE id = ?
                  AND deleted_at IS NULL
                  AND picked_at IS NULL
                """;

        jdbc.update(sql, note, itemId);
    }

    public static class PickItemRow {
        private final Long id;
        private final Long orderItemId;
        private final String titleSnapshot;
        private final String skuSnapshot;
        private final String copyCodeSnapshot;
        private final String locationSnapshot;
        private final Integer quantity;
        private final LocalDateTime pickedAt;
        private final boolean missingReported;
        private final String missingNote;

        public PickItemRow(Long id,
                           Long orderItemId,
                           String titleSnapshot,
                           String skuSnapshot,
                           String copyCodeSnapshot,
                           String locationSnapshot,
                           Integer quantity,
                           LocalDateTime pickedAt,
                           boolean missingReported,
                           String missingNote) {
            this.id = id;
            this.orderItemId = orderItemId;
            this.titleSnapshot = titleSnapshot;
            this.skuSnapshot = skuSnapshot;
            this.copyCodeSnapshot = copyCodeSnapshot;
            this.locationSnapshot = locationSnapshot;
            this.quantity = quantity;
            this.pickedAt = pickedAt;
            this.missingReported = missingReported;
            this.missingNote = missingNote;
        }

        public Long getId() {
            return id;
        }

        public Long getOrderItemId() {
            return orderItemId;
        }

        public String getTitleSnapshot() {
            return titleSnapshot;
        }

        public String getSkuSnapshot() {
            return skuSnapshot;
        }

        public String getCopyCodeSnapshot() {
            return copyCodeSnapshot;
        }

        public String getLocationSnapshot() {
            return locationSnapshot;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public LocalDateTime getPickedAt() {
            return pickedAt;
        }

        public boolean isMissingReported() {
            return missingReported;
        }

        public String getMissingNote() {
            return missingNote;
        }
    }
}