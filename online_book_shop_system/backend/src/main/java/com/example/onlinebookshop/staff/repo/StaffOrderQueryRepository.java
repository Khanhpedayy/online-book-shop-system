package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderItemRow;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.onlinebookshop.staff.dto.AllocatePreviewRow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import com.example.onlinebookshop.staff.dto.ReturnScanMatchRow;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

@Repository
public class StaffOrderQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffOrderQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderListRow> findOrders(OrderFilter filter, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP (:limit) ")
                .append("  o.id, o.order_code, o.status, o.payment_status, ")
                .append("  o.ship_name, o.ship_phone, o.total_amount, o.placed_at, o.shipped_at, o.delivered_at, o.completed_at, o.cancelled_at, ")
                .append("  COALESCE(agg.item_count, 0) AS item_count, ")
                .append("  COALESCE(agg.allocated_count, 0) AS allocated_count, ")
                .append("  COALESCE(agg.picked_count, 0) AS picked_count ")
                .append("FROM dbo.[orders] o ")
                .append("LEFT JOIN ( ")
                .append("  SELECT oi.order_id, ")
                .append("         COUNT(*) AS item_count, ")
                .append("         SUM(CASE WHEN oi.copy_id IS NOT NULL OR oi.picked_at IS NOT NULL THEN 1 ELSE 0 END) AS allocated_count, ")
                .append("         SUM(CASE WHEN oi.picked_at IS NOT NULL THEN 1 ELSE 0 END) AS picked_count ")
                .append("  FROM dbo.order_items oi ")
                .append("  WHERE oi.deleted_at IS NULL ")
                .append("  GROUP BY oi.order_id ")
                .append(") agg ON agg.order_id = o.id ")
                .append("WHERE o.deleted_at IS NULL ");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", Math.max(1, Math.min(limit, 200)));

        if (filter != null) {
            if (notBlank(filter.getStage())) {
                switch (filter.getStage().trim().toLowerCase()) {
                    case "confirmed" -> sql.append(" AND o.status = 'CONFIRMED' ");
                    case "allocate" -> sql.append(" AND o.status = 'CONFIRMED' AND NOT EXISTS (SELECT 1 FROM dbo.stock_outs so WHERE so.order_id = o.id AND so.status = 'PICKED' AND so.deleted_at IS NULL) ");
                    case "packing" -> sql.append(" AND o.status = 'CONFIRMED' AND EXISTS (SELECT 1 FROM dbo.stock_outs so WHERE so.order_id = o.id AND so.status = 'PICKED' AND so.deleted_at IS NULL) ");
                    case "shipping" -> sql.append(" AND o.status = 'PACKED' ");
                    case "delivery-return" -> sql.append(" AND (o.status IN ('SHIPPED','COMPLETED','CANCELLED','DELIVERY_FAILED') OR o.shipped_at IS NOT NULL OR o.delivered_at IS NOT NULL) ");
                    default -> { }
                }
            }
            if (notBlank(filter.getStatus())) {
                sql.append(" AND o.status = :status ");
                params.addValue("status", filter.getStatus().trim().toUpperCase());
            }
            if (notBlank(filter.getPaymentStatus())) {
                sql.append(" AND o.payment_status = :paymentStatus ");
                params.addValue("paymentStatus", filter.getPaymentStatus().trim().toUpperCase());
            }
            if (notBlank(filter.getDelivery())) {
                switch (filter.getDelivery().trim().toUpperCase()) {
                    case "NOT_SHIPPED" -> sql.append(" AND o.shipped_at IS NULL ");
                    case "IN_TRANSIT" -> sql.append(" AND o.shipped_at IS NOT NULL AND o.delivered_at IS NULL ");
                    case "DELIVERED" -> sql.append(" AND o.delivered_at IS NOT NULL ");
                    case "RETURNED" -> sql.append(" AND o.status = 'CANCELLED' ");
                    default -> { }
                }
            }
            if (notBlank(filter.getQ())) {
                sql.append(" AND (o.order_code LIKE :q OR o.ship_name LIKE :q OR o.ship_phone LIKE :q) ");
                params.addValue("q", "%" + filter.getQ().trim() + "%");
            }
        }

        sql.append(" ORDER BY ");
        String sort = (filter == null) ? null : filter.getSort();
        if (notBlank(sort)) {
            switch (sort.trim()) {
                case "placedAtAsc" -> sql.append(" o.placed_at ASC ");
                case "totalDesc" -> sql.append(" o.total_amount DESC ");
                case "totalAsc" -> sql.append(" o.total_amount ASC ");
                default -> sql.append(" o.placed_at DESC ");
            }
        } else {
            sql.append(" o.placed_at DESC ");
        }

        return jdbc.query(sql.toString(), params, orderListRowMapper());
    }

    public OrderDetailView getOrderDetail(long orderId) {
        String sql = """
                SELECT
                  o.id, o.order_code, o.user_id,
                  o.status, o.payment_status,
                  o.subtotal_amount, o.shipping_fee, o.discount_amount, o.total_amount,
                  o.ship_name, o.ship_phone, o.ship_line1, o.ship_line2, o.ship_ward, o.ship_district, o.ship_city, o.ship_province, o.ship_method,
                  o.carrier, o.tracking_code,
                  o.customer_note, o.staff_note,
                  o.placed_at, o.confirmed_at, o.packed_at, o.shipped_at, o.delivered_at, o.completed_at, o.cancelled_at
                FROM dbo.[orders] o
                WHERE o.deleted_at IS NULL AND o.id = :id
                """;

        MapSqlParameterSource p = new MapSqlParameterSource("id", orderId);
        List<OrderDetailView> rows = jdbc.query(sql, p, orderDetailRowMapper());
        if (rows.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        OrderDetailView view = rows.get(0);
        view.getItems().addAll(getOrderItems(orderId));
        return view;
    }

    public List<OrderItemRow> getOrderItems(long orderId) {
        String sql = """
                SELECT
                  oi.id,
                  oi.sku_snapshot,
                  oi.title_snapshot,
                  oi.condition_snapshot,
                  oi.quantity,
                  oi.unit_price,
                  oi.copy_id,
                  oi.picked_at
                FROM dbo.order_items oi
                WHERE oi.deleted_at IS NULL AND oi.order_id = :orderId
                ORDER BY oi.id ASC
                """;

        return jdbc.query(sql, new MapSqlParameterSource("orderId", orderId), orderItemRowMapper());
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static RowMapper<OrderListRow> orderListRowMapper() {
        return (rs, rowNum) -> {
            OrderListRow o = new OrderListRow();
            o.setId(rs.getLong("id"));
            o.setOrderCode(rs.getString("order_code"));
            o.setStatus(rs.getString("status"));
            o.setPaymentStatus(rs.getString("payment_status"));
            o.setShipName(rs.getString("ship_name"));
            o.setShipPhone(rs.getString("ship_phone"));
            o.setTotalAmount(rs.getBigDecimal("total_amount"));
            o.setPlacedAt(rs.getTimestamp("placed_at") == null ? null : rs.getTimestamp("placed_at").toLocalDateTime());
            o.setShippedAt(rs.getTimestamp("shipped_at") == null ? null : rs.getTimestamp("shipped_at").toLocalDateTime());
            o.setDeliveredAt(rs.getTimestamp("delivered_at") == null ? null : rs.getTimestamp("delivered_at").toLocalDateTime());
            o.setCompletedAt(rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime());
            o.setCancelledAt(rs.getTimestamp("cancelled_at") == null ? null : rs.getTimestamp("cancelled_at").toLocalDateTime());
            o.setItemCount(rs.getInt("item_count"));
            o.setAllocatedCount(rs.getInt("allocated_count"));
            o.setPickedCount(rs.getInt("picked_count"));
            return o;
        };
    }

    private static RowMapper<OrderDetailView> orderDetailRowMapper() {
        return (rs, rowNum) -> {
            OrderDetailView v = new OrderDetailView();
            v.setId(rs.getLong("id"));
            v.setOrderCode(rs.getString("order_code"));
            v.setUserId(rs.getLong("user_id"));
            v.setStatus(rs.getString("status"));
            v.setPaymentStatus(rs.getString("payment_status"));
            v.setSubtotalAmount(rs.getBigDecimal("subtotal_amount"));
            v.setShippingFee(rs.getBigDecimal("shipping_fee"));
            v.setDiscountAmount(rs.getBigDecimal("discount_amount"));
            v.setTotalAmount(rs.getBigDecimal("total_amount"));
            v.setShipName(rs.getString("ship_name"));
            v.setShipPhone(rs.getString("ship_phone"));
            v.setShipLine1(rs.getString("ship_line1"));
            v.setShipLine2(rs.getString("ship_line2"));
            v.setShipWard(rs.getString("ship_ward"));
            v.setShipDistrict(rs.getString("ship_district"));
            v.setShipCity(rs.getString("ship_city"));
            v.setShipProvince(rs.getString("ship_province"));
            v.setShipMethod(rs.getString("ship_method"));
            v.setCarrier(rs.getString("carrier"));
            v.setTrackingCode(rs.getString("tracking_code"));
            v.setCustomerNote(rs.getString("customer_note"));
            v.setStaffNote(rs.getString("staff_note"));
            v.setPlacedAt(rs.getTimestamp("placed_at") == null ? null : rs.getTimestamp("placed_at").toLocalDateTime());
            v.setConfirmedAt(rs.getTimestamp("confirmed_at") == null ? null : rs.getTimestamp("confirmed_at").toLocalDateTime());
            v.setPackedAt(rs.getTimestamp("packed_at") == null ? null : rs.getTimestamp("packed_at").toLocalDateTime());
            v.setShippedAt(rs.getTimestamp("shipped_at") == null ? null : rs.getTimestamp("shipped_at").toLocalDateTime());
            v.setDeliveredAt(rs.getTimestamp("delivered_at") == null ? null : rs.getTimestamp("delivered_at").toLocalDateTime());
            v.setCompletedAt(rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime());
            v.setCancelledAt(rs.getTimestamp("cancelled_at") == null ? null : rs.getTimestamp("cancelled_at").toLocalDateTime());
            return v;
        };
    }

    private static RowMapper<OrderItemRow> orderItemRowMapper() {
        return (rs, rowNum) -> {
            OrderItemRow i = new OrderItemRow();
            i.setId(rs.getLong("id"));
            i.setSkuSnapshot(rs.getString("sku_snapshot"));
            i.setTitleSnapshot(rs.getString("title_snapshot"));
            i.setConditionSnapshot(rs.getString("condition_snapshot"));
            i.setQuantity(rs.getInt("quantity"));
            i.setUnitPrice(rs.getBigDecimal("unit_price"));
            Object copyId = rs.getObject("copy_id");
            i.setCopyId(copyId == null ? null : ((Number) copyId).longValue());

            java.sql.Timestamp pickedAt = rs.getTimestamp("picked_at");
            i.setPickedAt(pickedAt == null ? null : pickedAt.toLocalDateTime());

            return i;
        };
    }

    public void autoAllocateAllItems(Long orderId) {
        String sql = """
            UPDATE oi
            SET oi.copy_id = oi.id
            FROM dbo.order_items oi
            WHERE oi.order_id = :orderId
              AND oi.deleted_at IS NULL
              AND oi.copy_id IS NULL
            """;
        jdbc.update(sql, new MapSqlParameterSource("orderId", orderId));
    }

    public void markPickedAllAllocatedItems(Long orderId) {
        String sql = """
            UPDATE oi
            SET oi.picked_at = GETDATE()
            FROM dbo.order_items oi
            WHERE oi.order_id = :orderId
              AND oi.deleted_at IS NULL
              AND oi.copy_id IS NOT NULL
              AND oi.picked_at IS NULL
            """;
        jdbc.update(sql, new MapSqlParameterSource("orderId", orderId));
    }

    public int confirmAutoAllocateAndPick(Long orderId) {
        List<AllocatePreviewRow> preview = buildAllocatePreview(orderId);
        if (preview.isEmpty()) {
            return 0;
        }

        for (AllocatePreviewRow row : preview) {
            if (!"OK".equalsIgnoreCase(row.getStockStatus())) {
                throw new RuntimeException("Không đủ tồn kho để allocate cho đơn này.");
            }
        }

        int updated = 0;

        for (AllocatePreviewRow row : preview) {
            if ("PER_COPY".equalsIgnoreCase(row.getSellMode())) {
                String updateOrderItem = """
                UPDATE dbo.order_items
                SET copy_id = :copyId,
                    pick_method = 'AUTO',
                    picked_at = GETDATE(),
                    updated_at = GETDATE()
                WHERE id = :orderItemId
                  AND deleted_at IS NULL
                  AND copy_id IS NULL
                  AND picked_at IS NULL
                """;

                updated += jdbc.update(updateOrderItem, new MapSqlParameterSource()
                        .addValue("copyId", row.getCopyId())
                        .addValue("orderItemId", row.getOrderItemId()));

                String updateCopy = """
                UPDATE dbo.copies
                SET status = 'PICKED',
                    updated_at = GETDATE()
                WHERE id = :copyId
                  AND deleted_at IS NULL
                  AND status = 'AVAILABLE'
                """;

                jdbc.update(updateCopy, new MapSqlParameterSource("copyId", row.getCopyId()));

                String insertTxn = """
                INSERT INTO dbo.inventory_transactions
                    (movement_type, variant_id, copy_id, quantity, reference_type, reference_id, reason, note)
                VALUES
                    ('OUT', :variantId, :copyId, 1, 'ORDER', :orderId, 'SALE', N'Auto allocate + auto pick từ staff workspace')
                """;

                jdbc.update(insertTxn, new MapSqlParameterSource()
                        .addValue("variantId", row.getVariantId())
                        .addValue("copyId", row.getCopyId())
                        .addValue("orderId", orderId));
            } else {
                String updateOrderItem = """
                UPDATE dbo.order_items
                SET pick_method = 'AUTO',
                    picked_at = GETDATE(),
                    updated_at = GETDATE()
                WHERE id = :orderItemId
                  AND deleted_at IS NULL
                  AND picked_at IS NULL
                """;

                updated += jdbc.update(updateOrderItem, new MapSqlParameterSource()
                        .addValue("orderItemId", row.getOrderItemId()));

                String updateLot = """
                UPDATE l
                SET l.qty_available = l.qty_available - oi.quantity,
                    l.qty_sold = l.qty_sold + oi.quantity,
                    l.updated_at = GETDATE()
                FROM dbo.lots l
                JOIN dbo.order_items oi ON oi.id = :orderItemId
                WHERE l.id = (
                    SELECT TOP 1 l1.id
                    FROM dbo.lots l1
                    WHERE l1.deleted_at IS NULL
                      AND l1.variant_id = oi.variant_id
                      AND l1.status = 'RELEASED'
                      AND l1.qty_available >= oi.quantity
                    ORDER BY l1.received_at ASC, l1.id ASC
                )
                """;

                jdbc.update(updateLot, new MapSqlParameterSource("orderItemId", row.getOrderItemId()));

                String insertTxn = """
                INSERT INTO dbo.inventory_transactions
                    (movement_type, variant_id, quantity, reference_type, reference_id, reason, note)
                SELECT
                    'OUT',
                    oi.variant_id,
                    oi.quantity,
                    'ORDER',
                    :orderId,
                    'SALE',
                    N'Auto allocate + auto pick từ staff workspace'
                FROM dbo.order_items oi
                WHERE oi.id = :orderItemId
                """;

                jdbc.update(insertTxn, new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("orderItemId", row.getOrderItemId()));
            }
        }

        return updated;
    }

    public List<AllocatePreviewRow> buildAllocatePreview(Long orderId) {
        String sql = """
        SELECT
            oi.id AS order_item_id,
            oi.variant_id,
            oi.title_snapshot,
            oi.sku_snapshot,
            b.sell_mode,

            c.id AS copy_id,
            c.copy_code,
            c.location,
            c.condition_grade,

            l.lot_id,
            l.lot_location,
            ISNULL(l.lot_qty_available, 0) AS lot_qty_available
        FROM dbo.order_items oi
        JOIN dbo.book_variants v
          ON v.id = oi.variant_id
         AND v.deleted_at IS NULL
        JOIN dbo.books b
          ON b.id = v.book_id
         AND b.deleted_at IS NULL

        OUTER APPLY (
            SELECT TOP 1
                c1.id,
                c1.copy_code,
                c1.location,
                c1.condition_grade
            FROM dbo.copies c1
            WHERE c1.deleted_at IS NULL
              AND c1.variant_id = oi.variant_id
              AND c1.status = 'AVAILABLE'
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.order_items oi2
                  WHERE oi2.deleted_at IS NULL
                    AND oi2.copy_id = c1.id
              )
            ORDER BY c1.id ASC
        ) c

        OUTER APPLY (
            SELECT TOP 1
                l1.id AS lot_id,
                l1.warehouse AS lot_location,
                l1.qty_available AS lot_qty_available
            FROM dbo.lots l1
            WHERE l1.deleted_at IS NULL
              AND l1.variant_id = oi.variant_id
              AND l1.status = 'RELEASED'
              AND l1.qty_available >= oi.quantity
            ORDER BY l1.received_at ASC, l1.id ASC
        ) l

        WHERE oi.deleted_at IS NULL
          AND oi.order_id = :orderId
          AND oi.picked_at IS NULL
        ORDER BY oi.id ASC
        """;

        List<AllocatePreviewRow> rows = jdbc.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> {
                    AllocatePreviewRow r = new AllocatePreviewRow();
                    r.setOrderItemId(rs.getLong("order_item_id"));
                    r.setVariantId(rs.getLong("variant_id"));
                    r.setTitleSnapshot(rs.getString("title_snapshot"));
                    r.setSkuSnapshot(rs.getString("sku_snapshot"));
                    r.setSellMode(rs.getString("sell_mode"));

                    String sellMode = rs.getString("sell_mode");

                    if ("PER_COPY".equalsIgnoreCase(sellMode)) {
                        Object copyId = rs.getObject("copy_id");
                        r.setCopyId(copyId == null ? null : ((Number) copyId).longValue());
                        r.setCopyCode(rs.getString("copy_code"));
                        r.setLocation(rs.getString("location"));
                        r.setConditionGrade(rs.getString("condition_grade"));
                        r.setStockStatus(copyId != null ? "OK" : "OUT_OF_STOCK");
                    } else {
                        int lotQtyAvailable = rs.getInt("lot_qty_available");
                        String lotLocation = rs.getString("lot_location");

                        r.setCopyId(null);
                        r.setCopyCode(lotQtyAvailable > 0 ? "Theo lô" : null);
                        r.setLocation(lotLocation);
                        r.setConditionGrade(lotQtyAvailable > 0 ? "QTY" : null);
                        r.setStockStatus(lotQtyAvailable > 0 ? "OK" : "OUT_OF_STOCK");
                    }

                    return r;
                }
        );

        Set<Long> used = new HashSet<>();
        List<AllocatePreviewRow> result = new ArrayList<>();

        for (AllocatePreviewRow row : rows) {
            if (!"PER_COPY".equalsIgnoreCase(row.getSellMode())) {
                result.add(row);
                continue;
            }

            if (row.getCopyId() == null) {
                result.add(row);
                continue;
            }

            if (used.contains(row.getCopyId())) {
                row.setCopyId(null);
                row.setCopyCode(null);
                row.setLocation(null);
                row.setConditionGrade(null);
                row.setStockStatus("OUT_OF_STOCK");
            } else {
                used.add(row.getCopyId());
            }
            result.add(row);
        }

        return result;
    }

    public ReturnScanMatchRow findReturnedCopyInOrder(Long orderId, String copyCode) {
        String sql = """
            SELECT TOP 1
                o.id AS order_id,
                oi.id AS order_item_id,
                oi.variant_id,
                o.order_code,
                oi.title_snapshot,
                oi.sku_snapshot,
                c.id AS copy_id,
                c.copy_code,
                c.status AS current_copy_status,
                c.condition_grade AS current_condition_grade
            FROM dbo.orders o
            JOIN dbo.order_items oi
              ON oi.order_id = o.id
             AND oi.deleted_at IS NULL
            JOIN dbo.copies c
              ON c.id = oi.copy_id
             AND c.deleted_at IS NULL
            WHERE o.deleted_at IS NULL
              AND o.id = :orderId
              AND c.copy_code = :copyCode
            """;

        try {
            return jdbc.queryForObject(sql,
                    new MapSqlParameterSource()
                            .addValue("orderId", orderId)
                            .addValue("copyCode", copyCode),
                    (rs, rowNum) -> {
                        ReturnScanMatchRow r = new ReturnScanMatchRow();
                        r.setOrderId(rs.getLong("order_id"));
                        r.setOrderItemId(rs.getLong("order_item_id"));
                        r.setVariantId(rs.getLong("variant_id"));
                        r.setOrderCode(rs.getString("order_code"));
                        r.setTitleSnapshot(rs.getString("title_snapshot"));
                        r.setSkuSnapshot(rs.getString("sku_snapshot"));
                        r.setCopyId(rs.getLong("copy_id"));
                        r.setCopyCode(rs.getString("copy_code"));
                        r.setCurrentCopyStatus(rs.getString("current_copy_status"));
                        r.setCurrentConditionGrade(rs.getString("current_condition_grade"));
                        return r;
                    });
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public Long createReturnHeader(Long orderId, String reason, String note) {
        String sql = """
            INSERT INTO dbo.returns
                (return_code, order_id, status, reason, note, received_at, created_at, updated_at)
            OUTPUT INSERTED.id
            VALUES
                (
                    CONCAT('RET-', FORMAT(GETDATE(),'yyyyMMddHHmmss'), '-', ABS(CHECKSUM(NEWID())) % 10000),
                    :orderId,
                    'RECEIVED',
                    :reason,
                    :note,
                    GETDATE(),
                    GETDATE(),
                    GETDATE()
                )
            """;

        return jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("reason", reason)
                        .addValue("note", note),
                Long.class);
    }

    public void createReturnItem(Long returnId,
                                 Long orderItemId,
                                 Long copyId,
                                 String receivedConditionGrade,
                                 String receivedConditionNote,
                                 String action) {
        String sql = """
            INSERT INTO dbo.return_items
                (return_id, order_item_id, copy_id, quantity,
                 received_condition_grade, received_condition_note, action, processed_at)
            VALUES
                (:returnId, :orderItemId, :copyId, 1,
                 :receivedConditionGrade, :receivedConditionNote, :action, GETDATE())
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("returnId", returnId)
                .addValue("orderItemId", orderItemId)
                .addValue("copyId", copyId)
                .addValue("receivedConditionGrade", receivedConditionGrade)
                .addValue("receivedConditionNote", receivedConditionNote)
                .addValue("action", action));
    }

    public void updateCopyAfterReturn(Long copyId, String newStatus, String newConditionGrade, String conditionNote) {
        String sql = """
            UPDATE dbo.copies
            SET status = :newStatus,
                condition_grade = COALESCE(:newConditionGrade, condition_grade),
                condition_note = :conditionNote,
                updated_at = GETDATE()
            WHERE id = :copyId
              AND deleted_at IS NULL
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("copyId", copyId)
                .addValue("newStatus", newStatus)
                .addValue("newConditionGrade", newConditionGrade)
                .addValue("conditionNote", conditionNote));
    }

    public void insertReturnInventoryTransaction(Long variantId,
                                                 Long copyId,
                                                 Long returnId,
                                                 String note) {
        String sql = """
            INSERT INTO dbo.inventory_transactions
                (movement_type, variant_id, copy_id, quantity, reference_type, reference_id, reason, note, created_at)
            VALUES
                ('RETURN', :variantId, :copyId, 1, 'RETURN', :returnId, 'FOUND', :note, GETDATE())
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("variantId", variantId)
                .addValue("copyId", copyId)
                .addValue("returnId", returnId)
                .addValue("note", note));
    }

    public boolean existsReturnItemByCopyId(Long copyId) {
        String sql = """
            SELECT COUNT(1)
            FROM dbo.return_items ri
            WHERE ri.copy_id = :copyId
            """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("copyId", copyId),
                Integer.class
        );

        return count != null && count > 0;
    }
}