package com.example.onlinebookshop.paymentlog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ManagerPaymentLogRepository {
    private final JdbcTemplate jdbc;

    public ManagerPaymentLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertPayOsLink(long orderId, String paymentLinkId, BigDecimal amount, int payosOrderCode) {
        String tx = paymentLinkId != null && !paymentLinkId.isBlank() ? paymentLinkId : "UNKNOWN";
        String raw = "{\"payosOrderCode\":" + payosOrderCode + ",\"orderId\":" + orderId + "}";
        jdbc.update(
                "INSERT INTO payment_logs (order_id, provider, transaction_id, amount, status, raw_data) VALUES (?,?,?,?,?,?)",
                orderId, "PAYOS", tx, amount, "PENDING", raw
        );
    }

    public List<PaymentLogDTO> findByOrderId(Long orderId) {
        return jdbc.query(
                "SELECT pl.id, pl.order_id, o.order_code, pl.provider, pl.transaction_id, pl.amount, "
                        + "pl.status, pl.flagged, pl.flag_reason, pl.created_at "
                        + "FROM payment_logs pl JOIN orders o ON pl.order_id=o.id WHERE pl.order_id=? ORDER BY pl.created_at DESC",
                (rs, i) -> {
                    PaymentLogDTO d = new PaymentLogDTO();
                    d.setId(rs.getLong("id"));
                    d.setOrderId(rs.getLong("order_id"));
                    d.setOrderCode(rs.getString("order_code"));
                    d.setProvider(rs.getString("provider"));
                    d.setTransactionId(rs.getString("transaction_id"));
                    d.setAmount(rs.getDouble("amount"));
                    d.setStatus(rs.getString("status"));
                    d.setFlagged(rs.getBoolean("flagged"));
                    d.setFlagReason(rs.getString("flag_reason"));
                    if (rs.getTimestamp("created_at") != null)
                        d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                    return d;
                }, orderId);
    }

    public int flagPayment(Long id, String reason) {
        return jdbc.update("UPDATE payment_logs SET flagged=1, flag_reason=? WHERE id=?", reason, id);
    }

    /* simulate recheck â€” in real implementation would call PayOS API */
    public String recheck(Long id) {
        List<String> statuses = jdbc.query("SELECT status FROM payment_logs WHERE id=?",
                (rs, i) -> rs.getString("status"), id);
        return statuses.isEmpty() ? null : statuses.get(0);
    }
}

