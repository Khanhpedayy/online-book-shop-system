package com.example.onlinebookshop.paymentlog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentLogRepository {
    private final JdbcTemplate jdbc;

    public PaymentLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsPayosPaymentLogForOrder(Long orderId, String paymentLinkId) {
        if (orderId == null) {
            return false;
        }
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            return false;
        }
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM payment_logs WHERE order_id=? AND provider='PAYOS' AND transaction_id=?",
                Integer.class,
                orderId,
                paymentLinkId.trim()
        );
        return count != null && count > 0;
    }

    /**
     * Orders tied to this PayOS payment link, before {@link #syncPaymentStatusByPaymentLinkId} runs.
     */
    public List<PayOsAffectedOrderRow> findOrdersLinkedToPayosPaymentLink(String paymentLinkId) {
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            return Collections.emptyList();
        }
        return jdbc.query(
                """
                        SELECT DISTINCT o.id, o.payment_method, o.payment_status
                        FROM dbo.orders o
                        INNER JOIN dbo.payment_logs pl ON pl.order_id = o.id
                        WHERE pl.provider = 'PAYOS'
                          AND pl.transaction_id = ?
                          AND o.deleted_at IS NULL
                        """,
                (rs, i) -> new PayOsAffectedOrderRow(
                        rs.getLong("id"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status")),
                paymentLinkId.trim());
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

    /**
     * Sync payment result into:
     * - payment_logs.status
     * - orders.payment_status
     *
     * @param paymentLinkId PayOS paymentLinkId (we store it into payment_logs.transaction_id)
     * @param status Target status: "PAID" or "UNPAID"
     */
    public void syncPaymentStatusByPaymentLinkId(String paymentLinkId, String status) {
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            throw new IllegalArgumentException("paymentLinkId is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }

        String normalized = status.trim().toUpperCase();

        // SQL Server can require SET QUOTED_IDENTIFIER for some update/from queries,
        // otherwise it fails with:
        // "UPDATE failed because the following SET options have incorrect settings: 'QUOTED_IDENTIFIER' ..."
        jdbc.execute("SET QUOTED_IDENTIFIER ON");

        int updatedLogs = jdbc.update(
                "UPDATE payment_logs SET status=? WHERE provider='PAYOS' AND transaction_id=?",
                normalized, paymentLinkId.trim()
        );

        // Update orders based on all logs that match paymentLinkId
        int updatedOrders = jdbc.update(
                "UPDATE o SET o.payment_status=? " +
                        "FROM dbo.[orders] o " +
                        "WHERE o.id IN (SELECT pl.order_id FROM dbo.payment_logs pl WHERE pl.provider='PAYOS' AND pl.transaction_id=?)",
                normalized, paymentLinkId.trim()
        );

        if (updatedLogs == 0 && updatedOrders == 0) {
            throw new RuntimeException("Payment not found for paymentLinkId: " + paymentLinkId);
        }
    }

    /* simulate recheck â€” in real implementation would call PayOS API */
    public String recheck(Long id) {
        List<String> statuses = jdbc.query("SELECT status FROM payment_logs WHERE id=?",
                (rs, i) -> rs.getString("status"), id);
        return statuses.isEmpty() ? null : statuses.get(0);
    }
}

