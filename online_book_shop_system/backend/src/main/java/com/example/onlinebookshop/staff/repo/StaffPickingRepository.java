package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffPickingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffPickingRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderItemInfo> findOrderItemsByOrderId(long orderId) {
        String sql = """
            SELECT
                oi.id,
                oi.order_id,
                oi.variant_id,
                oi.copy_id,
                oi.quantity,
                oi.pick_method,
                oi.picked_at
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
            ORDER BY oi.id
            """;

        return jdbc.query(sql, new MapSqlParameterSource("oid", orderId), (rs, rowNum) -> {
            Timestamp pickedAt = rs.getTimestamp("picked_at");
            Long copyId = rs.getObject("copy_id") == null ? null : rs.getLong("copy_id");
            return new OrderItemInfo(
                    rs.getLong("id"),
                    rs.getLong("order_id"),
                    rs.getLong("variant_id"),
                    copyId,
                    rs.getInt("quantity"),
                    rs.getString("pick_method"),
                    pickedAt == null ? null : pickedAt.toLocalDateTime()
            );
        });
    }

    public Optional<OrderItemInfo> findOrderItemInfo(long orderId, long orderItemId) {
        String sql = """
            SELECT TOP 1
                oi.id,
                oi.order_id,
                oi.variant_id,
                oi.copy_id,
                oi.quantity,
                oi.pick_method,
                oi.picked_at
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :orderId
              AND oi.id = :orderItemId
            """;

        List<OrderItemInfo> rows = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("orderItemId", orderItemId),
                (rs, rowNum) -> {
                    Timestamp pickedAt = rs.getTimestamp("picked_at");
                    Long copyId = rs.getObject("copy_id") == null ? null : rs.getLong("copy_id");
                    return new OrderItemInfo(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("variant_id"),
                            copyId,
                            rs.getInt("quantity"),
                            rs.getString("pick_method"),
                            pickedAt == null ? null : pickedAt.toLocalDateTime()
                    );
                });

        return rows.stream().findFirst();
    }

    public Optional<OrderItemInfo> findOrderItemInfoByItemId(long orderItemId) {
        String sql = """
            SELECT TOP 1
                oi.id,
                oi.order_id,
                oi.variant_id,
                oi.copy_id,
                oi.quantity,
                oi.pick_method,
                oi.picked_at
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.id = :orderItemId
            """;

        List<OrderItemInfo> rows = jdbc.query(sql,
                new MapSqlParameterSource("orderItemId", orderItemId),
                (rs, rowNum) -> {
                    Timestamp pickedAt = rs.getTimestamp("picked_at");
                    Long copyId = rs.getObject("copy_id") == null ? null : rs.getLong("copy_id");
                    return new OrderItemInfo(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getLong("variant_id"),
                            copyId,
                            rs.getInt("quantity"),
                            rs.getString("pick_method"),
                            pickedAt == null ? null : pickedAt.toLocalDateTime()
                    );
                });

        return rows.stream().findFirst();
    }

    public List<Long> findUnallocatedOrderItemIds(long orderId) {
        String sql = """
            SELECT oi.id
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id IS NULL
              AND oi.quantity = 1
            ORDER BY oi.id ASC
            """;

        return jdbc.queryForList(sql, new MapSqlParameterSource("oid", orderId), Long.class);
    }

    public long getVariantIdByOrderItemId(long orderItemId) {
        String sql = """
            SELECT oi.variant_id
            FROM dbo.order_items oi
            WHERE oi.id = :id
              AND oi.deleted_at IS NULL
            """;

        Long value = jdbc.queryForObject(sql, new MapSqlParameterSource("id", orderItemId), Long.class);
        if (value == null) {
            throw new IllegalArgumentException("Order item not found: " + orderItemId);
        }
        return value;
    }

    public Optional<Long> findFifoAvailableCopyId(long variantId) {
        String sql = """
            SELECT TOP 1 c.id
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL
              AND c.variant_id = :vid
              AND c.status = 'AVAILABLE'
            ORDER BY c.lot_id ASC, c.created_at ASC, c.id ASC
            """;

        List<Long> ids = jdbc.queryForList(
                sql,
                new MapSqlParameterSource("vid", variantId),
                Long.class
        );

        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public List<CopyChoice> findAvailableCopiesByOrderItemId(long orderItemId) {
        String sql = """
            SELECT
                c.id,
                c.copy_code,
                c.location,
                c.condition_grade,
                c.lot_id,
                l.lot_code
            FROM dbo.order_items oi
            JOIN dbo.copies c
              ON c.variant_id = oi.variant_id
             AND c.deleted_at IS NULL
             AND c.status = 'AVAILABLE'
            JOIN dbo.lots l
              ON l.id = c.lot_id
             AND l.deleted_at IS NULL
            WHERE oi.id = :orderItemId
              AND oi.deleted_at IS NULL
            ORDER BY c.location, c.copy_code
            """;

        return jdbc.query(sql,
                new MapSqlParameterSource("orderItemId", orderItemId),
                (rs, rowNum) -> new CopyChoice(
                        rs.getLong("id"),
                        rs.getString("copy_code"),
                        rs.getString("location"),
                        rs.getString("condition_grade"),
                        rs.getLong("lot_id"),
                        rs.getString("lot_code")
                ));
    }

    public List<LotChoice> findAvailableLotsByOrderItemId(long orderItemId) {
        String sql = """
            SELECT
                l.id,
                l.lot_code,
                l.qty_available,
                l.condition_default,
                l.warehouse
            FROM dbo.order_items oi
            JOIN dbo.lots l
              ON l.variant_id = oi.variant_id
             AND l.deleted_at IS NULL
             AND l.status = 'RELEASED'
             AND l.qty_available > 0
            WHERE oi.id = :orderItemId
              AND oi.deleted_at IS NULL
            ORDER BY l.received_at, l.id
            """;

        return jdbc.query(sql,
                new MapSqlParameterSource("orderItemId", orderItemId),
                (rs, rowNum) -> new LotChoice(
                        rs.getLong("id"),
                        rs.getString("lot_code"),
                        rs.getInt("qty_available"),
                        rs.getString("condition_default"),
                        rs.getString("warehouse")
                ));
    }

    public Optional<CopyInfo> findAvailableCopyById(long copyId) {
        String sql = """
            SELECT TOP 1
                c.id,
                c.variant_id,
                c.lot_id,
                c.copy_code,
                c.status
            FROM dbo.copies c
            WHERE c.id = :id
              AND c.deleted_at IS NULL
            """;

        List<CopyInfo> rows = jdbc.query(sql,
                new MapSqlParameterSource("id", copyId),
                (rs, rowNum) -> new CopyInfo(
                        rs.getLong("id"),
                        rs.getLong("variant_id"),
                        rs.getLong("lot_id"),
                        rs.getString("copy_code"),
                        rs.getString("status")
                ));

        return rows.stream().findFirst();
    }

    public Optional<CopyInfo> findCopyByCode(String copyCode) {
        String sql = """
            SELECT TOP 1
                c.id,
                c.variant_id,
                c.lot_id,
                c.copy_code,
                c.status
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL
              AND c.copy_code = :code
            """;

        List<CopyInfo> rows = jdbc.query(sql,
                new MapSqlParameterSource("code", copyCode),
                (rs, rowNum) -> new CopyInfo(
                        rs.getLong("id"),
                        rs.getLong("variant_id"),
                        rs.getLong("lot_id"),
                        rs.getString("copy_code"),
                        rs.getString("status")
                ));

        return rows.stream().findFirst();
    }

    public Optional<LotInfo> findAvailableLotByIdForOrderItem(long orderItemId, long lotId) {
        String sql = """
            SELECT TOP 1
                l.id,
                l.variant_id,
                l.lot_code,
                l.qty_available,
                l.condition_default,
                l.warehouse
            FROM dbo.order_items oi
            JOIN dbo.lots l
              ON l.variant_id = oi.variant_id
             AND l.deleted_at IS NULL
             AND l.status = 'RELEASED'
            WHERE oi.id = :orderItemId
              AND oi.deleted_at IS NULL
              AND l.id = :lotId
            """;

        List<LotInfo> rows = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("orderItemId", orderItemId)
                        .addValue("lotId", lotId),
                (rs, rowNum) -> new LotInfo(
                        rs.getLong("id"),
                        rs.getLong("variant_id"),
                        rs.getString("lot_code"),
                        rs.getInt("qty_available"),
                        rs.getString("condition_default"),
                        rs.getString("warehouse")
                ));

        return rows.stream().findFirst();
    }

    public Optional<BoundCopyDisplay> findBoundCopyDisplayByCopyId(long copyId) {
        String sql = """
            SELECT TOP 1
                c.id,
                c.copy_code,
                c.location,
                c.condition_grade,
                c.lot_id,
                l.lot_code
            FROM dbo.copies c
            LEFT JOIN dbo.lots l
              ON l.id = c.lot_id
             AND l.deleted_at IS NULL
            WHERE c.id = :copyId
              AND c.deleted_at IS NULL
            """;

        List<BoundCopyDisplay> rows = jdbc.query(sql,
                new MapSqlParameterSource("copyId", copyId),
                (rs, rowNum) -> new BoundCopyDisplay(
                        rs.getLong("id"),
                        rs.getString("copy_code"),
                        rs.getString("location"),
                        rs.getString("condition_grade"),
                        rs.getObject("lot_id") == null ? null : rs.getLong("lot_id"),
                        rs.getString("lot_code")
                ));

        return rows.stream().findFirst();
    }

    public int reserveCopy(long copyId, int ttlMinutes) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'RESERVED',
                reserved_at = SYSUTCDATETIME(),
                reserve_expires_at = DATEADD(minute, :ttl, SYSUTCDATETIME()),
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
              AND status = 'AVAILABLE'
            """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", copyId)
                .addValue("ttl", ttlMinutes));
    }

    public int markCopyPicked(long copyId) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'PICKED',
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('AVAILABLE', 'RESERVED', 'PICKED')
            """;

        return jdbc.update(sql, new MapSqlParameterSource("id", copyId));
    }

    public int releaseCopy(long copyId) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'AVAILABLE',
                reserved_at = NULL,
                reserve_expires_at = NULL,
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('RESERVED', 'PICKED')
            """;

        return jdbc.update(sql, new MapSqlParameterSource("id", copyId));
    }

    public int assignCopyToOrderItem(long orderItemId, long copyId, String pickMethod) {
        String sql = """
            UPDATE dbo.order_items
            SET copy_id = :cid,
                pick_method = :pm,
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
              AND copy_id IS NULL
              AND quantity = 1
            """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderItemId)
                .addValue("cid", copyId)
                .addValue("pm", pickMethod));
    }

    public int clearPickAndAssignment(long orderItemId) {
        String sql = """
            UPDATE dbo.order_items
            SET copy_id = NULL,
                pick_method = NULL,
                picked_by = NULL,
                picked_at = NULL,
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource("id", orderItemId));
    }

    public int markOrderItemPicked(long orderItemId, String pickMethod) {
        String sql = """
            UPDATE dbo.order_items
            SET pick_method = :pm,
                picked_at = COALESCE(picked_at, SYSUTCDATETIME()),
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderItemId)
                .addValue("pm", pickMethod));
    }

    public int reserveLotCountersByCopy(long copyId) {
        String sql = """
            UPDATE l
            SET l.qty_available = l.qty_available - 1,
                l.qty_reserved = l.qty_reserved + 1,
                l.updated_at = GETDATE()
            FROM dbo.lots l
            JOIN dbo.copies c ON c.lot_id = l.id
            WHERE c.id = :copyId
              AND l.deleted_at IS NULL
              AND l.qty_available > 0
            """;

        return jdbc.update(sql, new MapSqlParameterSource("copyId", copyId));
    }

    public int releaseLotCountersByCopy(long copyId) {
        String sql = """
            UPDATE l
            SET l.qty_available = l.qty_available + 1,
                l.qty_reserved = CASE WHEN l.qty_reserved > 0 THEN l.qty_reserved - 1 ELSE 0 END,
                l.updated_at = GETDATE()
            FROM dbo.lots l
            JOIN dbo.copies c ON c.lot_id = l.id
            WHERE c.id = :copyId
              AND l.deleted_at IS NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource("copyId", copyId));
    }

    public int releaseLotCountersByOrderId(long orderId) {
        String sql = """
            UPDATE l
            SET l.qty_available = l.qty_available + 1,
                l.qty_reserved = CASE WHEN l.qty_reserved > 0 THEN l.qty_reserved - 1 ELSE 0 END,
                l.updated_at = GETDATE()
            FROM dbo.lots l
            JOIN dbo.copies c ON c.lot_id = l.id
            JOIN dbo.order_items oi ON oi.copy_id = c.id
            WHERE oi.order_id = :orderId
              AND oi.deleted_at IS NULL
              AND oi.copy_id IS NOT NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource("orderId", orderId));
    }

    public int releaseAllCopiesByOrderId(long orderId) {
        String sql = """
            UPDATE c
            SET c.status = 'AVAILABLE',
                c.reserved_at = NULL,
                c.reserve_expires_at = NULL,
                c.updated_at = GETDATE()
            FROM dbo.copies c
            JOIN dbo.order_items oi ON oi.copy_id = c.id
            WHERE oi.order_id = :orderId
              AND oi.deleted_at IS NULL
              AND c.deleted_at IS NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource("orderId", orderId));
    }

    public int clearAllPicksByOrderId(long orderId) {
        String sql = """
            UPDATE dbo.order_items
            SET copy_id = NULL,
                pick_method = NULL,
                picked_by = NULL,
                picked_at = NULL,
                updated_at = GETDATE()
            WHERE order_id = :orderId
              AND deleted_at IS NULL
            """;

        return jdbc.update(sql, new MapSqlParameterSource("orderId", orderId));
    }

    public Long createReservedCopyFromLot(long lotId, long variantId, String copyCode) {
        String sql = """
            INSERT INTO dbo.copies (
                copy_code,
                lot_id,
                variant_id,
                location,
                condition_grade,
                status,
                reserved_at,
                reserve_expires_at,
                created_at,
                updated_at
            )
            OUTPUT INSERTED.id
            SELECT
                :copyCode,
                l.id,
                :variantId,
                'A1-01',
                l.condition_default,
                'RESERVED',
                SYSUTCDATETIME(),
                DATEADD(minute, 30, SYSUTCDATETIME()),
                SYSUTCDATETIME(),
                GETDATE()
            FROM dbo.lots l
            WHERE l.id = :lotId
              AND l.deleted_at IS NULL
              AND l.status = 'RELEASED'
              AND l.qty_available > 0
            """;

        List<Long> rows = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("lotId", lotId)
                        .addValue("variantId", variantId)
                        .addValue("copyCode", copyCode),
                (rs, rowNum) -> rs.getLong(1));

        return rows.isEmpty() ? null : rows.get(0);
    }

    public int reserveLotCountersByLot(long lotId) {
        String sql = """
            UPDATE dbo.lots
            SET qty_available = qty_available - 1,
                qty_reserved = qty_reserved + 1,
                updated_at = GETDATE()
            WHERE id = :lotId
              AND deleted_at IS NULL
              AND status = 'RELEASED'
              AND qty_available > 0
            """;

        return jdbc.update(sql, new MapSqlParameterSource("lotId", lotId));
    }

    public void insertInventoryTxReserve(long variantId, long lotId, long copyId, long orderId, String note) {
        String sql = """
            INSERT INTO dbo.inventory_transactions
                (movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reference_id, reason, note)
            VALUES
                ('RESERVE', :vid, :lotId, :cid, 1, 'ORDER', :oid, 'SALE', :note)
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("vid", variantId)
                .addValue("lotId", lotId)
                .addValue("cid", copyId)
                .addValue("oid", orderId)
                .addValue("note", note));
    }

    public boolean canSplitOrderItem(long orderId, long orderItemId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.order_items
            WHERE id = :id
              AND order_id = :orderId
              AND deleted_at IS NULL
              AND quantity > 1
              AND copy_id IS NULL
              AND picked_at IS NULL
            """;

        Integer count = jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("id", orderItemId)
                        .addValue("orderId", orderId),
                Integer.class);

        return count != null && count > 0;
    }

    public int splitOrderItemToSingleUnits(long orderId, long orderItemId) {
        String readSql = """
            SELECT TOP 1
                oi.id,
                oi.order_id,
                oi.variant_id,
                oi.title_snapshot,
                oi.sku_snapshot,
                oi.condition_snapshot,
                oi.unit_price,
                oi.quantity
            FROM dbo.order_items oi
            WHERE oi.id = :id
              AND oi.order_id = :orderId
              AND oi.deleted_at IS NULL
              AND oi.quantity > 1
              AND oi.copy_id IS NULL
              AND oi.picked_at IS NULL
            """;

        List<SplitSeed> seeds = jdbc.query(readSql,
                new MapSqlParameterSource()
                        .addValue("id", orderItemId)
                        .addValue("orderId", orderId),
                (rs, rowNum) -> new SplitSeed(
                        rs.getLong("id"),
                        rs.getLong("order_id"),
                        rs.getLong("variant_id"),
                        rs.getString("title_snapshot"),
                        rs.getString("sku_snapshot"),
                        rs.getString("condition_snapshot"),
                        rs.getBigDecimal("unit_price"),
                        rs.getInt("quantity")
                ));

        if (seeds.isEmpty()) {
            return 0;
        }

        SplitSeed seed = seeds.get(0);

        String updateOriginalSql = """
            UPDATE dbo.order_items
            SET quantity = 1,
                updated_at = GETDATE()
            WHERE id = :id
              AND deleted_at IS NULL
            """;

        jdbc.update(updateOriginalSql, new MapSqlParameterSource("id", seed.id()));

        String insertSql = """
            INSERT INTO dbo.order_items (
                order_id,
                variant_id,
                copy_id,
                title_snapshot,
                sku_snapshot,
                condition_snapshot,
                unit_price,
                quantity,
                created_at,
                updated_at
            )
            VALUES (
                :orderId,
                :variantId,
                NULL,
                :titleSnapshot,
                :skuSnapshot,
                :conditionSnapshot,
                :unitPrice,
                1,
                SYSUTCDATETIME(),
                GETDATE()
            )
            """;

        int inserted = 0;
        for (int i = 1; i < seed.quantity(); i++) {
            inserted += jdbc.update(insertSql,
                    new MapSqlParameterSource()
                            .addValue("orderId", seed.orderId())
                            .addValue("variantId", seed.variantId())
                            .addValue("titleSnapshot", seed.titleSnapshot())
                            .addValue("skuSnapshot", seed.skuSnapshot())
                            .addValue("conditionSnapshot", seed.conditionSnapshot())
                            .addValue("unitPrice", seed.unitPrice()));
        }

        return inserted + 1;
    }

    public record OrderItemInfo(
            long id,
            long orderId,
            long variantId,
            Long copyId,
            int quantity,
            String pickMethod,
            LocalDateTime pickedAt
    ) {}

    public record CopyInfo(
            long id,
            long variantId,
            long lotId,
            String copyCode,
            String status
    ) {}

    public record LotInfo(
            long id,
            long variantId,
            String lotCode,
            int qtyAvailable,
            String conditionDefault,
            String warehouse
    ) {}

    public record CopyChoice(
            long id,
            String copyCode,
            String location,
            String conditionGrade,
            long lotId,
            String lotCode
    ) {}

    public record LotChoice(
            long id,
            String lotCode,
            int qtyAvailable,
            String conditionDefault,
            String warehouse
    ) {}

    public record BoundCopyDisplay(
            long id,
            String copyCode,
            String location,
            String conditionGrade,
            Long lotId,
            String lotCode
    ) {}

    private record SplitSeed(
            long id,
            long orderId,
            long variantId,
            String titleSnapshot,
            String skuSnapshot,
            String conditionSnapshot,
            java.math.BigDecimal unitPrice,
            int quantity
    ) {}
}