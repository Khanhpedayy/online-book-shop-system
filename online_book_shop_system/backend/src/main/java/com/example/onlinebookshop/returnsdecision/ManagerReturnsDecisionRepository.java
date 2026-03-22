package com.example.onlinebookshop.returnsdecision;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ManagerReturnsDecisionRepository {

    private final JdbcTemplate jdbc;

    public ManagerReturnsDecisionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReturnOverviewDTO> findAllReturns() {
        String sql = "SELECT r.id, r.return_code, r.order_id, o.order_code, r.status, r.reason, r.note, "
                + "r.refund_amount, u1.full_name AS requested_by, u2.full_name AS approved_by, r.created_at "
                + "FROM returns r "
                + "JOIN orders o ON r.order_id = o.id "
                + "LEFT JOIN users u1 ON r.requested_by = u1.id "
                + "LEFT JOIN users u2 ON r.approved_by = u2.id "
                + "WHERE r.deleted_at IS NULL "
                + "ORDER BY r.created_at DESC";
        List<ReturnOverviewDTO> returns = jdbc.query(sql, (rs, i) -> {
            ReturnOverviewDTO d = new ReturnOverviewDTO();
            d.setReturnId(rs.getLong("id"));
            d.setReturnCode(rs.getString("return_code"));
            d.setOrderId(rs.getLong("order_id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setStatus(rs.getString("status"));
            d.setReason(rs.getString("reason"));
            d.setNote(rs.getString("note"));
            d.setRefundAmount(rs.getObject("refund_amount") != null ? rs.getDouble("refund_amount") : null);
            d.setRequestedBy(rs.getString("requested_by"));
            d.setApprovedBy(rs.getString("approved_by"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        });

        // Load items for each return
        for (ReturnOverviewDTO ret : returns) {
            ret.setItems(findItemsByReturn(ret.getReturnId()));
        }
        return returns;
    }

    public List<ReturnItemDTO> findItemsByReturn(Long returnId) {
        String sql = "SELECT ri.id, ri.return_id, ri.order_item_id, ri.copy_id, c.copy_code, "
                + "ri.quantity, ri.received_condition_grade, ri.received_condition_note, "
                + "ri.action, u.full_name AS processed_by, ri.processed_at, "
                + "oi.title_snapshot, oi.sku_snapshot "
                + "FROM return_items ri "
                + "JOIN order_items oi ON ri.order_item_id = oi.id "
                + "LEFT JOIN copies c ON ri.copy_id = c.id "
                + "LEFT JOIN users u ON ri.processed_by = u.id "
                + "WHERE ri.return_id = ?";
        return jdbc.query(sql, (rs, i) -> {
            ReturnItemDTO d = new ReturnItemDTO();
            d.setId(rs.getLong("id"));
            d.setReturnId(rs.getLong("return_id"));
            d.setOrderItemId(rs.getLong("order_item_id"));
            d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
            d.setCopyCode(rs.getString("copy_code"));
            d.setQuantity(rs.getInt("quantity"));
            d.setReceivedConditionGrade(rs.getString("received_condition_grade"));
            d.setReceivedConditionNote(rs.getString("received_condition_note"));
            d.setAction(rs.getString("action"));
            d.setProcessedBy(rs.getString("processed_by"));
            if (rs.getTimestamp("processed_at") != null)
                d.setProcessedAt(rs.getTimestamp("processed_at").toLocalDateTime().toString());
            d.setTitleSnapshot(rs.getString("title_snapshot"));
            d.setSkuSnapshot(rs.getString("sku_snapshot"));
            return d;
        }, returnId);
    }

    public int processReturnItem(Long itemId, String action) {
        return jdbc.update(
                "UPDATE return_items SET action = ?, processed_at = SYSUTCDATETIME() WHERE id = ?",
                action, itemId);
    }

    public void updateCopyStatus(Long copyId, String status) {
        jdbc.update("UPDATE copies SET status = ?, updated_at = SYSUTCDATETIME() WHERE id = ?", status, copyId);
    }

    public void updateCopyCondition(Long copyId, String conditionGrade) {
        jdbc.update("UPDATE copies SET condition_grade = ?, updated_at = SYSUTCDATETIME() WHERE id = ?", conditionGrade,
                copyId);
    }

    public void updateCopyPriceOverride(Long copyId, Double newPrice) {
        jdbc.update("UPDATE copies SET sell_price_override = ?, updated_at = SYSUTCDATETIME() WHERE id = ?", newPrice,
                copyId);
    }

    public void logTransaction(String movementType, Long variantId, Long lotId, Long copyId,
            int qty, String refType, Long refId, String reason, String note) {
        jdbc.update("INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, copy_id, "
                + "quantity, reference_type, reference_id, reason, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                movementType, variantId, lotId, copyId, qty, refType, refId, reason, note);
    }
}

