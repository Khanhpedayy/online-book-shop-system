package com.example.onlinebookshop.returnsintake;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ManagerReturnsIntakeRepository {
    private final JdbcTemplate jdbc;

    public ManagerReturnsIntakeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReturnIntakeDTO> findAll() {
        String sql = "SELECT r.id, r.return_code, r.order_id, o.order_code, r.status, r.reason, "
                + "r.note, r.refund_amount, r.created_at "
                + "FROM returns r JOIN orders o ON r.order_id=o.id ORDER BY r.created_at DESC";
        List<ReturnIntakeDTO> list = jdbc.query(sql, (rs, i) -> {
            ReturnIntakeDTO d = new ReturnIntakeDTO();
            d.setId(rs.getLong("id"));
            d.setReturnCode(rs.getString("return_code"));
            d.setOrderId(rs.getLong("order_id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setStatus(rs.getString("status"));
            d.setReason(rs.getString("reason"));
            d.setNote(rs.getString("note"));
            d.setRefundAmount(rs.getDouble("refund_amount"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        });
        for (ReturnIntakeDTO r : list)
            r.setItems(findItemsByReturnId(r.getId()));
        return list;
    }

    public List<ReturnIntakeItemDTO> findItemsByReturnId(Long returnId) {
        String sql = "SELECT ri.id, ri.order_item_id, ri.copy_id, c.copy_code, ri.quantity, "
                + "ri.received_condition_grade, ri.received_condition_note, ri.action, "
                + "oi.title_snapshot, oi.sku_snapshot "
                + "FROM return_items ri "
                + "LEFT JOIN copies c ON ri.copy_id=c.id "
                + "JOIN order_items oi ON ri.order_item_id=oi.id "
                + "WHERE ri.return_id=?";
        return jdbc.query(sql, (rs, i) -> {
            ReturnIntakeItemDTO d = new ReturnIntakeItemDTO();
            d.setId(rs.getLong("id"));
            d.setOrderItemId(rs.getLong("order_item_id"));
            d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
            d.setCopyCode(rs.getString("copy_code"));
            d.setQuantity(rs.getInt("quantity"));
            d.setReceivedConditionGrade(rs.getString("received_condition_grade"));
            d.setReceivedConditionNote(rs.getString("received_condition_note"));
            d.setAction(rs.getString("action"));
            d.setTitleSnapshot(rs.getString("title_snapshot"));
            d.setSkuSnapshot(rs.getString("sku_snapshot"));
            return d;
        }, returnId);
    }

    public Long createReturn(CreateReturnIntakeRequest req) {
        String code = "RET-" + System.currentTimeMillis();
        jdbc.update("INSERT INTO returns (return_code, order_id, status, reason, note, refund_amount, requested_by) "
                + "VALUES (?,?,?,?,?,?,?)", code, req.getOrderId(), "INTAKE", req.getReason(), req.getNote(),
                req.getRefundAmount(), req.getRequestedBy());
        return jdbc.queryForObject("SELECT TOP 1 id FROM returns WHERE return_code=?", Long.class, code);
    }

    public void scanReturnCopy(Long returnId, ScanReturnCopyRequest req) {
        Long copyId = null;
        if (req.getCopyCode() != null && !req.getCopyCode().isEmpty()) {
            List<Long> ids = jdbc.query("SELECT id FROM copies WHERE copy_code=?",
                    (rs, i) -> rs.getLong("id"), req.getCopyCode());
            copyId = ids.isEmpty() ? null : ids.get(0);
        }
        jdbc.update("INSERT INTO return_items (return_id, order_item_id, copy_id, quantity) VALUES (?,?,?,1)",
                returnId, req.getOrderItemId(), copyId);
    }

    public int recordCondition(Long itemId, RecordConditionRequest req) {
        return jdbc.update("UPDATE return_items SET received_condition_grade=?, received_condition_note=? WHERE id=?",
                req.getConditionGrade(), req.getConditionNote(), itemId);
    }

    public int escalateToManager(Long returnId) {
        return jdbc.update("UPDATE returns SET status='PENDING_DECISION' WHERE id=?", returnId);
    }
}

