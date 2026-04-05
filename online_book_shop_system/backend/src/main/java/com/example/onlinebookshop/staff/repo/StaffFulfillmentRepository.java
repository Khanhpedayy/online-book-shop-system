package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffFulfillmentRepository {

    private final JdbcTemplate jdbc;

    public StaffFulfillmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PackingQueueRow> findPackingQueue(String q, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT
                o.id AS order_id,
                o.order_code,
                o.ship_name,
                o.ship_phone,
                o.ship_city,
                o.ship_district,
                o.payment_status,
                o.total_amount,
                o.shipping_fee,
                o.discount_amount,
                so.id AS stock_out_id,
                so.stock_out_code,
                so.status AS stock_out_status,
                so.has_exception,
                (
                    SELECT COUNT(*)
                    FROM dbo.stock_out_items soi
                    WHERE soi.stock_out_id = so.id
                      AND soi.deleted_at IS NULL
                ) AS item_count
            FROM dbo.stock_outs so
            JOIN dbo.orders o
              ON o.id = so.order_id
             AND o.deleted_at IS NULL
            WHERE so.deleted_at IS NULL
              AND so.status = 'PICKED'
              AND so.has_exception = 0
            """);

        if (q != null && !q.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR o.ship_name LIKE ? OR o.ship_phone LIKE ?) ");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ? ");
        }

        sql.append(" ORDER BY so.id DESC ");

        java.util.List<Object> args = new java.util.ArrayList<>();
        if (q != null && !q.isBlank()) {
            String search = "%" + q.trim() + "%";
            args.add(search);
            args.add(search);
            args.add(search);
        }
        if (status != null && !status.isBlank()) {
            args.add(status);
        }

        return jdbc.query(sql.toString(), (rs, rowNum) -> new PackingQueueRow(
                rs.getLong("order_id"),
                java.util.Optional.ofNullable(rs.getString("order_code")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("ship_name")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("ship_phone")).orElse(""),
                joinArea(rs.getString("ship_district"), rs.getString("ship_city")),
                java.util.Optional.ofNullable(rs.getString("payment_status")).orElse(""),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("discount_amount"),
                rs.getLong("stock_out_id"),
                java.util.Optional.ofNullable(rs.getString("stock_out_code")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("stock_out_status")).orElse(""),
                rs.getBoolean("has_exception"),
                rs.getInt("item_count")
        ), args.toArray());
    }

    public Optional<PackDetailView> findPackDetail(long orderId) {
        String headerSql = """
            SELECT TOP 1
                o.id AS order_id,
                o.order_code,
                o.status AS order_status,
                o.payment_status,
                o.ship_name,
                o.ship_phone,
                o.ship_line1,
                o.ship_line2,
                o.ship_ward,
                o.ship_district,
                o.ship_city,
                o.ship_province,
                o.customer_note,
                o.staff_note,
                o.subtotal_amount,
                o.shipping_fee,
                o.discount_amount,
                o.total_amount,
                o.packed_at,
                so.id AS stock_out_id,
                so.stock_out_code,
                so.status AS stock_out_status,
                so.has_exception,
                so.note AS stock_out_note
            FROM dbo.orders o
            JOIN dbo.stock_outs so
              ON so.order_id = o.id
             AND so.deleted_at IS NULL
            WHERE o.id = ?
              AND o.deleted_at IS NULL
            ORDER BY so.id DESC
            """;

        List<PackDetailView> headers = jdbc.query(headerSql, (rs, rowNum) -> new PackDetailView(
                rs.getLong("order_id"),
                rs.getString("order_code"),
                rs.getString("order_status"),
                rs.getString("payment_status"),
                rs.getString("ship_name"),
                rs.getString("ship_phone"),
                rs.getString("ship_line1"),
                rs.getString("ship_line2"),
                rs.getString("ship_ward"),
                rs.getString("ship_district"),
                rs.getString("ship_city"),
                rs.getString("ship_province"),
                rs.getString("customer_note"),
                rs.getString("staff_note"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("total_amount"),
                toLocalDateTime(rs, "packed_at"),
                rs.getLong("stock_out_id"),
                rs.getString("stock_out_code"),
                rs.getString("stock_out_status"),
                rs.getBoolean("has_exception"),
                rs.getString("stock_out_note"),
                List.of()
        ), orderId);

        if (headers.isEmpty()) {
            return Optional.empty();
        }

        PackDetailView header = headers.get(0);
        List<PackItemRow> items = findPackItems(header.stockOutId());

        return Optional.of(header.withItems(items));
    }

    public List<PackItemRow> findPackItems(long stockOutId) {
        String sql = """
            SELECT
                soi.id,
                soi.order_item_id,
                soi.title_snapshot,
                soi.sku_snapshot,
                soi.copy_code_snapshot,
                soi.location_snapshot,
                soi.quantity,
                soi.picked_at
            FROM dbo.stock_out_items soi
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
            ORDER BY soi.id
            """;

        return jdbc.query(sql, (rs, rowNum) -> new PackItemRow(
                rs.getLong("id"),
                rs.getLong("order_item_id"),
                rs.getString("title_snapshot"),
                rs.getString("sku_snapshot"),
                rs.getString("copy_code_snapshot"),
                rs.getString("location_snapshot"),
                rs.getInt("quantity"),
                toLocalDateTime(rs, "picked_at")
        ), stockOutId);
    }

    public boolean canPack(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.stock_outs so
            WHERE so.order_id = ?
              AND so.deleted_at IS NULL
              AND so.status = 'PICKED'
              AND so.has_exception = 0
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.stock_out_items soi
                  WHERE soi.stock_out_id = so.id
                    AND soi.deleted_at IS NULL
                    AND (soi.picked_at IS NULL OR soi.is_missing_reported = 1)
              )
            """;

        Integer count = jdbc.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }

    public long findLatestStockOutIdByOrderId(long orderId) {
        String sql = """
            SELECT TOP 1 id
            FROM dbo.stock_outs
            WHERE order_id = ?
              AND deleted_at IS NULL
            ORDER BY id DESC
            """;

        Long value = jdbc.queryForObject(sql, Long.class, orderId);
        if (value == null) {
            throw new RuntimeException("Không tìm thấy phiếu xuất.");
        }
        return value;
    }

    public void markOrderPacked(long orderId, Long userId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'PACKED',
                packed_at = COALESCE(packed_at, GETDATE()),
                packed_by = COALESCE(packed_by, ?),
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, userId, orderId);
    }

    public void markCopiesPackedByStockOut(long stockOutId) {
        String sql = """
            UPDATE c
            SET c.status = 'PACKED',
                c.updated_at = GETDATE()
            FROM dbo.copies c
            JOIN dbo.stock_out_items soi ON soi.copy_id = c.id
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
              AND c.deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void markStockOutPacked(long stockOutId, Long userId) {
        String sql = """
            UPDATE dbo.stock_outs
            SET status = 'PACKED',
                packed_at = COALESCE(packed_at, GETDATE()),
                packed_by = COALESCE(packed_by, ?),
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, userId, stockOutId);
    }

    public List<ShippingQueueRow> findShippingQueue(String q, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT
                o.id AS order_id,
                o.order_code,
                o.status AS order_status,
                o.ship_name,
                o.ship_phone,
                o.ship_line1,
                o.ship_district,
                o.ship_city,
                o.payment_status,
                o.total_amount,
                o.shipping_fee,
                o.discount_amount,
                o.carrier,
                o.packed_at,
                so.id AS stock_out_id,
                so.stock_out_code,
                so.status AS stock_out_status
            FROM dbo.orders o
            JOIN dbo.stock_outs so
              ON so.order_id = o.id
             AND so.deleted_at IS NULL
            WHERE o.deleted_at IS NULL
              AND o.status IN ('PACKED', 'SHIPPED')
            """);

        java.util.List<Object> args = new java.util.ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR o.ship_name LIKE ? OR o.ship_phone LIKE ?) ");
            String search = "%" + q.trim() + "%";
            args.add(search);
            args.add(search);
            args.add(search);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ? ");
            args.add(status);
        }

        sql.append("""
            ORDER BY
                CASE WHEN o.status = 'PACKED' THEN 0 ELSE 1 END,
                o.packed_at DESC,
                o.id DESC
            """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new ShippingQueueRow(
                rs.getLong("order_id"),
                java.util.Optional.ofNullable(rs.getString("order_code")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("order_status")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("ship_name")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("ship_phone")).orElse(""),
                safeJoinAddress(
                        rs.getString("ship_line1"),
                        rs.getString("ship_district"),
                        rs.getString("ship_city")
                ),
                java.util.Optional.ofNullable(rs.getString("payment_status")).orElse(""),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("discount_amount"),
                toLocalDateTime(rs, "packed_at"),
                java.util.Optional.ofNullable(rs.getString("carrier")).orElse(""),
                rs.getLong("stock_out_id"),
                java.util.Optional.ofNullable(rs.getString("stock_out_code")).orElse(""),
                java.util.Optional.ofNullable(rs.getString("stock_out_status")).orElse("")
        ), args.toArray());
    }

    public Optional<DeliveryDetailView> findDeliveryDetail(long orderId) {
        String headerSql = """
            SELECT TOP 1
                o.id AS order_id,
                o.order_code,
                o.status AS order_status,
                o.payment_status,
                o.ship_name,
                o.ship_phone,
                o.ship_line1,
                o.ship_line2,
                o.ship_ward,
                o.ship_district,
                o.ship_city,
                o.ship_province,
                o.customer_note,
                o.staff_note,
                o.subtotal_amount,
                o.shipping_fee,
                o.discount_amount,
                o.total_amount,
                o.shipped_at,
                o.delivered_at,
                so.id AS stock_out_id,
                so.stock_out_code,
                so.status AS stock_out_status
            FROM dbo.orders o
            JOIN dbo.stock_outs so
              ON so.order_id = o.id
             AND so.deleted_at IS NULL
            WHERE o.id = ?
              AND o.deleted_at IS NULL
            ORDER BY so.id DESC
            """;

        List<DeliveryDetailView> headers = jdbc.query(headerSql, (rs, rowNum) -> new DeliveryDetailView(
                rs.getLong("order_id"),
                rs.getString("order_code"),
                rs.getString("order_status"),
                rs.getString("payment_status"),
                rs.getString("ship_name"),
                rs.getString("ship_phone"),
                rs.getString("ship_line1"),
                rs.getString("ship_line2"),
                rs.getString("ship_ward"),
                rs.getString("ship_district"),
                rs.getString("ship_city"),
                rs.getString("ship_province"),
                rs.getString("customer_note"),
                rs.getString("staff_note"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("shipping_fee"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("total_amount"),
                toLocalDateTime(rs, "shipped_at"),
                toLocalDateTime(rs, "delivered_at"),
                rs.getLong("stock_out_id"),
                rs.getString("stock_out_code"),
                rs.getString("stock_out_status"),
                List.of()
        ), orderId);

        if (headers.isEmpty()) {
            return Optional.empty();
        }

        DeliveryDetailView header = headers.get(0);
        List<DeliveryItemRow> items = findDeliveryItems(header.stockOutId());

        return Optional.of(header.withItems(items));
    }

    public List<DeliveryItemRow> findDeliveryItems(long stockOutId) {
        String sql = """
            SELECT
                soi.id,
                soi.title_snapshot,
                soi.sku_snapshot,
                soi.copy_code_snapshot,
                soi.quantity
            FROM dbo.stock_out_items soi
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
            ORDER BY soi.id
            """;

        return jdbc.query(sql, (rs, rowNum) -> new DeliveryItemRow(
                rs.getLong("id"),
                rs.getString("title_snapshot"),
                rs.getString("sku_snapshot"),
                rs.getString("copy_code_snapshot"),
                rs.getInt("quantity")
        ), stockOutId);
    }

    public boolean canShip(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.orders o
            JOIN dbo.stock_outs so ON so.order_id = o.id AND so.deleted_at IS NULL
            WHERE o.id = ?
              AND o.deleted_at IS NULL
              AND o.status = 'PACKED'
              AND so.status = 'PACKED'
              AND so.has_exception = 0
            """;

        Integer count = jdbc.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }

    public boolean canDeliver(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.orders o
            JOIN dbo.stock_outs so ON so.order_id = o.id AND so.deleted_at IS NULL
            WHERE o.id = ?
              AND o.deleted_at IS NULL
              AND o.status = 'SHIPPED'
              AND so.status = 'OUT_FOR_DELIVERY'
            """;

        Integer count = jdbc.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }

    public void markOrderShipped(long orderId, Long userId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'SHIPPED',
                shipped_at = COALESCE(shipped_at, GETDATE()),
                shipped_by = COALESCE(shipped_by, ?),
                carrier = 'SHOP_INTERNAL',
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, userId, orderId);
    }

    public void markCopiesShippedByStockOut(long stockOutId) {
        String sql = """
            UPDATE c
            SET c.status = 'SHIPPED',
                c.updated_at = GETDATE()
            FROM dbo.copies c
            JOIN dbo.stock_out_items soi ON soi.copy_id = c.id
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
              AND c.deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void markStockOutOutForDelivery(long stockOutId, Long userId) {
        String sql = """
            UPDATE dbo.stock_outs
            SET status = 'OUT_FOR_DELIVERY',
                delivered_at = COALESCE(delivered_at, GETDATE()),
                delivered_by = COALESCE(delivered_by, ?),
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, userId, stockOutId);
    }

    public void insertInventoryOutByStockOut(long stockOutId, long orderId) {
        String sql = """
            INSERT INTO dbo.inventory_transactions
                (movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reference_id, reason, note)
            SELECT
                'OUT',
                soi.variant_id,
                soi.lot_id,
                soi.copy_id,
                soi.quantity,
                'ORDER',
                ?,
                'DELIVERY',
                'Ship order by staff'
            FROM dbo.stock_out_items soi
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
            """;
        jdbc.update(sql, orderId, stockOutId);
    }

    public void markOrderDeliveredSuccess(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'DELIVERED',
                delivered_at = COALESCE(delivered_at, GETDATE()),
                completed_at = COALESCE(completed_at, GETDATE()),
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, orderId);
    }

    public void markOrderPaymentPaidIfNeeded(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET payment_status = 'PAID',
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
              AND payment_status <> 'PAID'
            """;
        jdbc.update(sql, orderId);
    }

    public void markCopiesSoldByStockOut(long stockOutId) {
        String sql = """
            UPDATE c
            SET c.status = 'SOLD',
                c.updated_at = GETDATE()
            FROM dbo.copies c
            JOIN dbo.stock_out_items soi ON soi.copy_id = c.id
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
              AND c.deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void moveLotReservedToSoldByStockOut(long stockOutId) {
        String sql = """
            ;WITH x AS (
                SELECT soi.lot_id, SUM(soi.quantity) AS qty
                FROM dbo.stock_out_items soi
                WHERE soi.stock_out_id = ?
                  AND soi.deleted_at IS NULL
                GROUP BY soi.lot_id
            )
            UPDATE l
            SET l.qty_reserved = CASE WHEN l.qty_reserved >= x.qty THEN l.qty_reserved - x.qty ELSE 0 END,
                l.qty_sold = l.qty_sold + x.qty,
                l.updated_at = GETDATE()
            FROM dbo.lots l
            JOIN x ON x.lot_id = l.id
            WHERE l.deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void markStockOutCompleted(long stockOutId) {
        String sql = """
            UPDATE dbo.stock_outs
            SET status = 'COMPLETED',
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void markOrderConfirmedForRetry(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'CONFIRMED',
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, orderId);
    }

    public void markOrderCancelled(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'CANCELLED',
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, orderId);
    }

    public void markCopiesReturnedOrAvailableByStockOut(long stockOutId, boolean asReturned) {
        String status = asReturned ? "RETURNED" : "AVAILABLE";
        String sql = """
            UPDATE c
            SET c.status = ?,
                c.updated_at = GETDATE()
            FROM dbo.copies c
            JOIN dbo.stock_out_items soi ON soi.copy_id = c.id
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
              AND c.deleted_at IS NULL
            """;
        jdbc.update(sql, status, stockOutId);
    }

    public void releaseLotReservedByStockOut(long stockOutId) {
        String sql = """
            ;WITH x AS (
                SELECT soi.lot_id, SUM(soi.quantity) AS qty
                FROM dbo.stock_out_items soi
                WHERE soi.stock_out_id = ?
                  AND soi.deleted_at IS NULL
                GROUP BY soi.lot_id
            )
            UPDATE l
            SET l.qty_reserved = CASE WHEN l.qty_reserved >= x.qty THEN l.qty_reserved - x.qty ELSE 0 END,
                l.qty_available = l.qty_available + x.qty,
                l.updated_at = GETDATE()
            FROM dbo.lots l
            JOIN x ON x.lot_id = l.id
            WHERE l.deleted_at IS NULL
            """;
        jdbc.update(sql, stockOutId);
    }

    public void markStockOutCancelled(long stockOutId, String note) {
        String sql = """
            UPDATE dbo.stock_outs
            SET status = 'CANCELLED',
                note = ?,
                updated_at = GETDATE()
            WHERE id = ?
              AND deleted_at IS NULL
            """;
        jdbc.update(sql, note, stockOutId);
    }

    public void insertInventoryReturnByStockOut(long stockOutId, long orderId, String note) {
        String sql = """
            INSERT INTO dbo.inventory_transactions
                (movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reference_id, reason, note)
            SELECT
                'RETURN',
                soi.variant_id,
                soi.lot_id,
                soi.copy_id,
                soi.quantity,
                'ORDER',
                ?,
                'DELIVERY_FAIL',
                ?
            FROM dbo.stock_out_items soi
            WHERE soi.stock_out_id = ?
              AND soi.deleted_at IS NULL
            """;
        jdbc.update(sql, orderId, note, stockOutId);
    }

    private static LocalDateTime toLocalDateTime(ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static String joinArea(String district, String city) {
        String d = district == null ? "" : district;
        String c = city == null ? "" : city;
        if (!d.isBlank() && !c.isBlank()) return d + " / " + c;
        return !d.isBlank() ? d : c;
    }

    private static String safeJoinAddress(String line1, String district, String city) {
        StringBuilder sb = new StringBuilder();
        if (line1 != null && !line1.isBlank()) sb.append(line1);
        if (district != null && !district.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (city != null && !city.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }

    public record PackingQueueRow(
            long orderId,
            String orderCode,
            String shipName,
            String shipPhone,
            String area,
            String paymentStatus,
            BigDecimal totalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            long stockOutId,
            String stockOutCode,
            String stockOutStatus,
            boolean hasException,
            int itemCount
    ) {}

    public record PackItemRow(
            long id,
            long orderItemId,
            String titleSnapshot,
            String skuSnapshot,
            String copyCodeSnapshot,
            String locationSnapshot,
            int quantity,
            LocalDateTime pickedAt
    ) {}

    public record PackDetailView(
            long orderId,
            String orderCode,
            String orderStatus,
            String paymentStatus,
            String shipName,
            String shipPhone,
            String shipLine1,
            String shipLine2,
            String shipWard,
            String shipDistrict,
            String shipCity,
            String shipProvince,
            String customerNote,
            String staffNote,
            BigDecimal subtotalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            LocalDateTime packedAt,
            long stockOutId,
            String stockOutCode,
            String stockOutStatus,
            boolean hasException,
            String stockOutNote,
            List<PackItemRow> items
    ) {
        public PackDetailView withItems(List<PackItemRow> newItems) {
            return new PackDetailView(
                    orderId, orderCode, orderStatus, paymentStatus,
                    shipName, shipPhone, shipLine1, shipLine2, shipWard, shipDistrict, shipCity, shipProvince,
                    customerNote, staffNote, subtotalAmount, shippingFee, discountAmount, totalAmount,
                    packedAt, stockOutId, stockOutCode, stockOutStatus, hasException, stockOutNote, newItems
            );
        }

        public BigDecimal codDue() {
            if ("PAID".equalsIgnoreCase(paymentStatus)) return BigDecimal.ZERO;
            return totalAmount == null ? BigDecimal.ZERO : totalAmount;
        }
    }

    public record ShippingQueueRow(
            long orderId,
            String orderCode,
            String orderStatus,
            String shipName,
            String shipPhone,
            String address,
            String paymentStatus,
            BigDecimal totalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            LocalDateTime packedAt,
            String carrier,
            long stockOutId,
            String stockOutCode,
            String stockOutStatus
    ) {
        public BigDecimal codDue() {
            if ("PAID".equalsIgnoreCase(paymentStatus)) return BigDecimal.ZERO;
            return totalAmount == null ? BigDecimal.ZERO : totalAmount;
        }
    }

    public record DeliveryItemRow(
            long id,
            String titleSnapshot,
            String skuSnapshot,
            String copyCodeSnapshot,
            int quantity
    ) {}

    public record DeliveryDetailView(
            long orderId,
            String orderCode,
            String orderStatus,
            String paymentStatus,
            String shipName,
            String shipPhone,
            String shipLine1,
            String shipLine2,
            String shipWard,
            String shipDistrict,
            String shipCity,
            String shipProvince,
            String customerNote,
            String staffNote,
            BigDecimal subtotalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            LocalDateTime shippedAt,
            LocalDateTime deliveredAt,
            long stockOutId,
            String stockOutCode,
            String stockOutStatus,
            List<DeliveryItemRow> items
    ) {
        public DeliveryDetailView withItems(List<DeliveryItemRow> newItems) {
            return new DeliveryDetailView(
                    orderId, orderCode, orderStatus, paymentStatus,
                    shipName, shipPhone, shipLine1, shipLine2, shipWard, shipDistrict, shipCity, shipProvince,
                    customerNote, staffNote, subtotalAmount, shippingFee, discountAmount, totalAmount,
                    shippedAt, deliveredAt, stockOutId, stockOutCode, stockOutStatus, newItems
            );
        }

        public BigDecimal codDue() {
            if ("PAID".equalsIgnoreCase(paymentStatus)) return BigDecimal.ZERO;
            return totalAmount == null ? BigDecimal.ZERO : totalAmount;
        }

        public String fullAddress() {
            StringBuilder sb = new StringBuilder();
            append(sb, shipLine1);
            append(sb, shipLine2);
            append(sb, shipWard);
            append(sb, shipDistrict);
            append(sb, shipCity);
            append(sb, shipProvince);
            return sb.toString();
        }

        private void append(StringBuilder sb, String v) {
            if (v != null && !v.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(v);
            }
        }
    }
}