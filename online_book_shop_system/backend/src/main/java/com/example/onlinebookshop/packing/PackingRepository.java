package com.example.onlinebookshop.packing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PackingRepository {

    private final JdbcTemplate jdbc;

    public PackingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PackingStatusDTO getStatus(Long orderId) {
        String sql = "SELECT o.id, o.order_code, o.packed_at, "
                + "(SELECT COUNT(*) FROM pick_list_items WHERE order_id=o.id) AS total_picks, "
                + "(SELECT COUNT(*) FROM pick_list_items WHERE order_id=o.id AND status='PICKED') AS picked_count, "
                + "COALESCE(s.box_count, 0) AS box_count "
                + "FROM orders o LEFT JOIN shipments s ON s.order_id=o.id WHERE o.id=?";
        List<PackingStatusDTO> list = jdbc.query(sql, (rs, i) -> {
            PackingStatusDTO d = new PackingStatusDTO();
            d.setOrderId(rs.getLong("id"));
            d.setOrderCode(rs.getString("order_code"));
            int total = rs.getInt("total_picks");
            int picked = rs.getInt("picked_count");
            d.setAllPicked(total > 0 && total == picked);
            d.setBoxCount(rs.getInt("box_count"));
            if (rs.getTimestamp("packed_at") != null) {
                d.setPackedAt(rs.getTimestamp("packed_at").toLocalDateTime().toString());
                d.setPackingConfirmed(true);
                d.setStatus("PACKED");
            } else if (total > 0 && total == picked) {
                d.setStatus("READY_TO_PACK");
            } else {
                d.setStatus("PENDING_PICK");
            }
            return d;
        }, orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    public PackingSlipDTO getPackingSlip(Long orderId) {
        String sql = "SELECT o.id, o.order_code, u.full_name, "
                + "CONCAT(o.ship_line1, ', ', o.ship_city) AS ship_address, "
                + "o.total_amount, o.currency, o.packed_at "
                + "FROM orders o LEFT JOIN users u ON o.user_id=u.id WHERE o.id=?";
        List<PackingSlipDTO> list = jdbc.query(sql, (rs, i) -> {
            PackingSlipDTO d = new PackingSlipDTO();
            d.setOrderId(rs.getLong("id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setCustomerName(rs.getString("full_name"));
            d.setShipAddress(rs.getString("ship_address"));
            d.setTotalAmount(rs.getDouble("total_amount"));
            d.setCurrency(rs.getString("currency"));
            if (rs.getTimestamp("packed_at") != null)
                d.setPackedAt(rs.getTimestamp("packed_at").toLocalDateTime().toString());
            return d;
        }, orderId);
        if (list.isEmpty())
            return null;
        PackingSlipDTO slip = list.get(0);
        slip.setItems(jdbc.query(
                "SELECT title_snapshot, sku_snapshot, quantity, unit_price, line_total FROM order_items WHERE order_id=?",
                (rs, i) -> {
                    PackingSlipItemDTO d = new PackingSlipItemDTO();
                    d.setTitle(rs.getString("title_snapshot"));
                    d.setSku(rs.getString("sku_snapshot"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getDouble("unit_price"));
                    d.setLineTotal(rs.getDouble("line_total"));
                    return d;
                }, orderId));
        return slip;
    }

    public int markPacked(Long orderId) {
        return jdbc.update("UPDATE orders SET packed_at=SYSUTCDATETIME(), status='PACKED' WHERE id=?", orderId);
    }
}

