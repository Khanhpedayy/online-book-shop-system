package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderItemRow;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repo dạng query (JDBC) cho màn staff (list/detail).
 *
 * Lý do dùng JDBC: query có filter optional + join, làm nhanh, không cần mở rộng Entity quá nhiều.
 */
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
                .append("  o.ship_name, o.ship_phone, o.total_amount, o.placed_at, o.shipped_at, o.delivered_at ")
                .append("FROM dbo.[orders] o ")
                .append("WHERE o.deleted_at IS NULL ");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", Math.max(1, Math.min(limit, 200)));

        if (filter != null) {
            if (notBlank(filter.getStatus())) {
                sql.append(" AND o.status = :status ");
                params.addValue("status", filter.getStatus().trim().toUpperCase());
            }
            if (notBlank(filter.getPaymentStatus())) {
                sql.append(" AND o.payment_status = :paymentStatus ");
                params.addValue("paymentStatus", filter.getPaymentStatus().trim().toUpperCase());
            }
            if (notBlank(filter.getDelivery())) {
                // delivery status: derive từ timeline
                switch (filter.getDelivery().trim().toUpperCase()) {
                    case "NOT_SHIPPED" -> sql.append(" AND o.shipped_at IS NULL ");
                    case "IN_TRANSIT" -> sql.append(" AND o.shipped_at IS NOT NULL AND o.delivered_at IS NULL ");
                    case "DELIVERED" -> sql.append(" AND o.delivered_at IS NOT NULL ");
                    case "RETURNED" -> sql.append(" AND o.status = 'CANCELLED' ");
                    default -> { /* ignore */ }
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
                  oi.copy_id
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
            return i;
        };
    }
}