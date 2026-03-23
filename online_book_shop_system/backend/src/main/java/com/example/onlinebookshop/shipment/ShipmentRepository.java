package com.example.onlinebookshop.shipment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ShipmentRepository {
    private final JdbcTemplate jdbc;

    public ShipmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ShipmentDTO findByOrderId(Long orderId) {
        List<ShipmentDTO> list = jdbc.query(
                "SELECT id, order_id, carrier, tracking_code, box_count, status, shipped_at, delivered_at, note, created_at "
                        + "FROM shipments WHERE order_id=? ORDER BY id DESC",
                (rs, i) -> {
                    ShipmentDTO d = new ShipmentDTO();
                    d.setId(rs.getLong("id"));
                    d.setOrderId(rs.getLong("order_id"));
                    d.setCarrier(rs.getString("carrier"));
                    d.setTrackingCode(rs.getString("tracking_code"));
                    d.setBoxCount(rs.getInt("box_count"));
                    d.setStatus(rs.getString("status"));
                    if (rs.getTimestamp("shipped_at") != null)
                        d.setShippedAt(rs.getTimestamp("shipped_at").toLocalDateTime().toString());
                    if (rs.getTimestamp("delivered_at") != null)
                        d.setDeliveredAt(rs.getTimestamp("delivered_at").toLocalDateTime().toString());
                    d.setNote(rs.getString("note"));
                    if (rs.getTimestamp("created_at") != null)
                        d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                    return d;
                }, orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void create(Long orderId, CreateShipmentRequest req) {
        jdbc.update("INSERT INTO shipments (order_id, carrier, tracking_code, box_count, note) VALUES (?,?,?,?,?)",
                orderId, req.getCarrier(), req.getTrackingCode(), req.getBoxCount(), req.getNote());
    }

    public int markShipped(Long orderId) {
        jdbc.update("UPDATE orders SET status='SHIPPED', delivery_status='IN_TRANSIT' WHERE id=?", orderId);
        return jdbc.update(
                "UPDATE shipments SET status='SHIPPED', shipped_at=SYSUTCDATETIME() WHERE order_id=? AND status='CREATED'",
                orderId);
    }

    public int updateDelivery(Long orderId, String outcome) {
        String orderStatus = "DELIVERED".equals(outcome) ? "COMPLETED" : "SHIPPED";
        String deliveryStatus = "DELIVERED".equals(outcome) ? "DELIVERED" : "RETURNED";
        String shipStatus = "DELIVERED".equals(outcome) ? "DELIVERED" : "FAILED";
        jdbc.update("UPDATE orders SET status=?, delivery_status=? WHERE id=?", orderStatus, deliveryStatus, orderId);
        return jdbc.update(
                "UPDATE shipments SET status=?, delivered_at=SYSUTCDATETIME() WHERE order_id=? AND status='SHIPPED'",
                shipStatus, orderId);
    }
}

