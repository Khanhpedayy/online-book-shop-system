package com.example.onlinebookshop.picking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ManagerPickingRepository {

    private final JdbcTemplate jdbc;

    public ManagerPickingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â”€â”€ Get pick items for an order â”€â”€ */
    public List<PickItemDTO> findByOrderId(Long orderId) {
        String sql = "SELECT p.id, p.order_item_id, p.copy_id, c.copy_code, p.location, p.status, "
                + "oi.title_snapshot, oi.sku_snapshot, c.condition_grade, p.picked_at, p.picked_by "
                + "FROM pick_list_items p "
                + "JOIN order_items oi ON p.order_item_id = oi.id "
                + "LEFT JOIN copies c ON p.copy_id = c.id "
                + "WHERE p.order_id = ? ORDER BY p.location, p.id";
        return jdbc.query(sql, (rs, i) -> {
            PickItemDTO d = new PickItemDTO();
            d.setId(rs.getLong("id"));
            d.setOrderItemId(rs.getLong("order_item_id"));
            d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
            d.setCopyCode(rs.getString("copy_code"));
            d.setLocation(rs.getString("location"));
            d.setStatus(rs.getString("status"));
            d.setTitleSnapshot(rs.getString("title_snapshot"));
            d.setSkuSnapshot(rs.getString("sku_snapshot"));
            d.setConditionGrade(rs.getString("condition_grade"));
            if (rs.getTimestamp("picked_at") != null)
                d.setPickedAt(rs.getTimestamp("picked_at").toLocalDateTime().toString());
            d.setPickedBy(rs.getObject("picked_by") != null ? rs.getLong("picked_by") : null);
            return d;
        }, orderId);
    }

    /* â”€â”€ Auto-allocate: FIFO by lot, pick oldest available copies â”€â”€ */
    public void autoAllocate(Long orderId) {
        // Get order items needing allocation
        String itemsSql = "SELECT oi.id AS order_item_id, oi.variant_id, oi.quantity "
                + "FROM order_items oi WHERE oi.order_id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM pick_list_items p WHERE p.order_item_id = oi.id)";
        jdbc.query(itemsSql, (rs, i) -> {
            Long orderItemId = rs.getLong("order_item_id");
            Long variantId = rs.getLong("variant_id");
            int qty = rs.getInt("quantity");

            // Find available copies via FIFO (oldest lot first)
            String copySql = "SELECT TOP " + qty + " c.id, c.location FROM copies c "
                    + "JOIN lots l ON c.lot_id = l.id "
                    + "WHERE c.variant_id = ? AND c.status = 'AVAILABLE' AND l.status = 'RELEASED' "
                    + "ORDER BY l.received_at ASC, c.id ASC";
            List<long[]> copies = jdbc.query(copySql, (rs2, j) -> new long[] { rs2.getLong("id") }, variantId);

            String locationSql = "SELECT TOP " + qty + " c.id, c.location FROM copies c "
                    + "JOIN lots l ON c.lot_id = l.id "
                    + "WHERE c.variant_id = ? AND c.status = 'AVAILABLE' AND l.status = 'RELEASED' "
                    + "ORDER BY l.received_at ASC, c.id ASC";
            jdbc.query(locationSql, (rs2, j) -> {
                jdbc.update(
                        "INSERT INTO pick_list_items (order_id, order_item_id, copy_id, location, status) VALUES (?,?,?,?,?)",
                        orderId, orderItemId, rs2.getLong("id"), rs2.getString("location"), "PENDING");
                // Reserve the copy
                jdbc.update("UPDATE copies SET status='RESERVED', reserved_at=SYSUTCDATETIME() WHERE id=?",
                        rs2.getLong("id"));
                return null;
            }, variantId);
            return null;
        }, orderId);
    }

    /* â”€â”€ Manual pick by copyCode â”€â”€ */
    public Long findCopyIdByCode(String copyCode) {
        List<Long> ids = jdbc.query("SELECT id FROM copies WHERE copy_code = ? AND status = 'AVAILABLE'",
                (rs, i) -> rs.getLong("id"), copyCode);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Long findCopyVariantId(Long copyId) {
        List<Long> ids = jdbc.query("SELECT variant_id FROM copies WHERE id = ?",
                (rs, i) -> rs.getLong("variant_id"), copyId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Long findOrderItemVariantId(Long orderItemId) {
        List<Long> ids = jdbc.query("SELECT variant_id FROM order_items WHERE id = ?",
                (rs, i) -> rs.getLong("variant_id"), orderItemId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public void insertPickItem(Long orderId, Long orderItemId, Long copyId, String location, Long staffId) {
        jdbc.update(
                "INSERT INTO pick_list_items (order_id, order_item_id, copy_id, location, status, picked_at, picked_by) "
                        + "VALUES (?,?,?,?,?,SYSUTCDATETIME(),?)",
                orderId, orderItemId, copyId, location, "PICKED", staffId);
        jdbc.update("UPDATE copies SET status='RESERVED', reserved_at=SYSUTCDATETIME() WHERE id=?", copyId);
    }

    public String getCopyLocation(Long copyId) {
        List<String> locs = jdbc.query("SELECT location FROM copies WHERE id=?", (rs, i) -> rs.getString("location"),
                copyId);
        return locs.isEmpty() ? null : locs.get(0);
    }

    /* â”€â”€ Confirm pick â”€â”€ */
    public int confirmPick(Long itemId, Long staffId) {
        return jdbc.update(
                "UPDATE pick_list_items SET status='PICKED', picked_at=SYSUTCDATETIME(), picked_by=? WHERE id=? AND status='PENDING'",
                staffId, itemId);
    }

    /* â”€â”€ Unpick â”€â”€ */
    public PickItemDTO findPickItemById(Long itemId) {
        List<PickItemDTO> list = jdbc
                .query("SELECT p.id, p.order_item_id, p.copy_id, c.copy_code, p.location, p.status, "
                        + "oi.title_snapshot, oi.sku_snapshot, c.condition_grade, p.picked_at, p.picked_by "
                        + "FROM pick_list_items p JOIN order_items oi ON p.order_item_id = oi.id "
                        + "LEFT JOIN copies c ON p.copy_id = c.id WHERE p.id = ?", (rs, i) -> {
                            PickItemDTO d = new PickItemDTO();
                            d.setId(rs.getLong("id"));
                            d.setOrderItemId(rs.getLong("order_item_id"));
                            d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
                            d.setCopyCode(rs.getString("copy_code"));
                            d.setLocation(rs.getString("location"));
                            d.setStatus(rs.getString("status"));
                            d.setTitleSnapshot(rs.getString("title_snapshot"));
                            d.setSkuSnapshot(rs.getString("sku_snapshot"));
                            d.setConditionGrade(rs.getString("condition_grade"));
                            if (rs.getTimestamp("picked_at") != null)
                                d.setPickedAt(rs.getTimestamp("picked_at").toLocalDateTime().toString());
                            d.setPickedBy(rs.getObject("picked_by") != null ? rs.getLong("picked_by") : null);
                            return d;
                        }, itemId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void unpick(Long itemId) {
        PickItemDTO item = findPickItemById(itemId);
        if (item != null && item.getCopyId() != null) {
            jdbc.update("UPDATE copies SET status='AVAILABLE', reserved_at=NULL, reserve_expires_at=NULL WHERE id=?",
                    item.getCopyId());
        }
        jdbc.update("DELETE FROM pick_list_items WHERE id=?", itemId);
    }

    public Long getOrderIdByPickItem(Long itemId) {
        List<Long> ids = jdbc.query("SELECT order_id FROM pick_list_items WHERE id=?",
                (rs, i) -> rs.getLong("order_id"), itemId);
        return ids.isEmpty() ? null : ids.get(0);
    }
}

