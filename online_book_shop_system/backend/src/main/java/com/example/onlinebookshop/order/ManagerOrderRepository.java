package com.example.onlinebookshop.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ManagerOrderRepository {

    private final JdbcTemplate jdbc;

    public ManagerOrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â”€â”€ List orders with filters â”€â”€ */
    public List<OrderListDTO> findAll(String status, String paymentStatus, String deliveryStatus,
                                      String search, String sortBy, String sortDir) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.order_code, u.full_name AS customer_name, u.phone AS customer_phone, "
                        + "o.status, o.payment_status, o.delivery_status, o.total_amount, o.currency, o.priority, "
                        + "o.sla_deadline, o.placed_at, o.created_at, "
                        + "(SELECT COUNT(*) FROM order_items oi WHERE oi.order_id = o.id) AS item_count "
                        + "FROM orders o "
                        + "LEFT JOIN users u ON o.user_id = u.id "
                        + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append("AND o.status = ? ");
            params.add(status);
        }
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            sql.append("AND o.payment_status = ? ");
            params.add(paymentStatus);
        }
        if (deliveryStatus != null && !deliveryStatus.isEmpty()) {
            sql.append("AND o.delivery_status = ? ");
            params.add(deliveryStatus);
        }
        if (search != null && !search.isEmpty()) {
            sql.append("AND (o.order_code LIKE ? OR u.full_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?) ");
            String pat = "%" + search + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        String sort = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "created_at";
        String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        switch (sort) {
            case "priority":
                sql.append("ORDER BY o.priority ").append(dir).append(", o.created_at DESC");
                break;
            case "placed_at":
                sql.append("ORDER BY o.placed_at ").append(dir);
                break;
            default:
                sql.append("ORDER BY o.created_at ").append(dir);
        }

        return jdbc.query(sql.toString(), (rs, i) -> {
            OrderListDTO d = new OrderListDTO();
            d.setId(rs.getLong("id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setCustomerName(rs.getString("customer_name"));
            d.setCustomerPhone(rs.getString("customer_phone"));
            d.setStatus(rs.getString("status"));
            d.setPaymentStatus(rs.getString("payment_status"));
            d.setDeliveryStatus(rs.getString("delivery_status"));
            d.setTotalAmount(rs.getDouble("total_amount"));
            d.setCurrency(rs.getString("currency"));
            d.setItemCount(rs.getInt("item_count"));
            d.setPriority(rs.getInt("priority"));
            if (rs.getTimestamp("sla_deadline") != null)
                d.setSlaDeadline(rs.getTimestamp("sla_deadline").toLocalDateTime().toString());
            if (rs.getTimestamp("placed_at") != null)
                d.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime().toString());
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, params.toArray());
    }

    /* â”€â”€ Single order detail â”€â”€ */
    public OrderDetailDTO findById(Long id) {
        String sql = "SELECT o.*, u.full_name AS customer_name, u.email AS customer_email, u.phone AS customer_phone "
                + "FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        List<OrderDetailDTO> list = jdbc.query(sql, (rs, i) -> {
            OrderDetailDTO d = new OrderDetailDTO();
            d.setId(rs.getLong("id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setUserId(rs.getLong("user_id"));
            d.setStatus(rs.getString("status"));
            d.setPaymentStatus(rs.getString("payment_status"));
            d.setDeliveryStatus(rs.getString("delivery_status"));
            d.setCurrency(rs.getString("currency"));
            d.setSubtotalAmount(rs.getDouble("subtotal_amount"));
            d.setTotalAmount(rs.getDouble("total_amount"));
            d.setPriority(rs.getInt("priority"));
            d.setShipName(rs.getString("ship_name"));
            d.setShipPhone(rs.getString("ship_phone"));
            d.setShipLine1(rs.getString("ship_line1"));
            try {
                d.setShipLine2(rs.getString("ship_line2"));
            } catch (Exception ignored) {
            }
            d.setShipCity(rs.getString("ship_city"));
            try {
                d.setShipDistrict(rs.getString("ship_district"));
            } catch (Exception ignored) {
            }
            try {
                d.setShipWard(rs.getString("ship_ward"));
            } catch (Exception ignored) {
            }
            d.setCustomerName(rs.getString("customer_name"));
            d.setCustomerEmail(rs.getString("customer_email"));
            d.setCustomerPhone(rs.getString("customer_phone"));
            if (rs.getTimestamp("sla_deadline") != null)
                d.setSlaDeadline(rs.getTimestamp("sla_deadline").toLocalDateTime().toString());
            if (rs.getTimestamp("placed_at") != null)
                d.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime().toString());
            if (rs.getTimestamp("confirmed_at") != null)
                d.setConfirmedAt(rs.getTimestamp("confirmed_at").toLocalDateTime().toString());
            if (rs.getTimestamp("packed_at") != null)
                d.setPackedAt(rs.getTimestamp("packed_at").toLocalDateTime().toString());
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, id);
        if (list.isEmpty())
            return null;
        OrderDetailDTO detail = list.get(0);
        detail.setItems(findItemsByOrderId(id));
        detail.setNotes(findNotesByOrderId(id));
        return detail;
    }

    /* â”€â”€ Order items â”€â”€ */
    public List<OrderItemDTO> findItemsByOrderId(Long orderId) {
        String sql = "SELECT oi.id, oi.variant_id, oi.copy_id, c.copy_code, oi.quantity, "
                + "oi.unit_price, oi.line_total, oi.title_snapshot, oi.sku_snapshot "
                + "FROM order_items oi LEFT JOIN copies c ON oi.copy_id = c.id "
                + "WHERE oi.order_id = ?";
        return jdbc.query(sql, (rs, i) -> {
            OrderItemDTO d = new OrderItemDTO();
            d.setId(rs.getLong("id"));
            d.setVariantId(rs.getLong("variant_id"));
            d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
            d.setCopyCode(rs.getString("copy_code"));
            d.setQuantity(rs.getInt("quantity"));
            d.setUnitPrice(rs.getDouble("unit_price"));
            d.setLineTotal(rs.getDouble("line_total"));
            d.setTitleSnapshot(rs.getString("title_snapshot"));
            d.setSkuSnapshot(rs.getString("sku_snapshot"));
            return d;
        }, orderId);
    }

    /* â”€â”€ Notes â”€â”€ */
    public List<OrderNoteDTO> findNotesByOrderId(Long orderId) {
        String sql = "SELECT n.id, n.staff_id, u.full_name AS staff_name, n.content, n.created_at "
                + "FROM order_notes n LEFT JOIN users u ON n.staff_id = u.id "
                + "WHERE n.order_id = ? ORDER BY n.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            OrderNoteDTO d = new OrderNoteDTO();
            d.setId(rs.getLong("id"));
            d.setStaffId(rs.getLong("staff_id"));
            d.setStaffName(rs.getString("staff_name"));
            d.setContent(rs.getString("content"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, orderId);
    }

    /* â”€â”€ Add note â”€â”€ */
    public void addNote(Long orderId, Long staffId, String content) {
        jdbc.update("INSERT INTO order_notes (order_id, staff_id, content) VALUES (?, ?, ?)",
                orderId, staffId, content);
    }

    /* â”€â”€ Confirm order â”€â”€ */
    public int confirmOrder(Long orderId) {
        return jdbc.update(
                "UPDATE orders SET status='CONFIRMED', confirmed_at=SYSUTCDATETIME() WHERE id=? AND status='NEW'",
                orderId);
    }
}

