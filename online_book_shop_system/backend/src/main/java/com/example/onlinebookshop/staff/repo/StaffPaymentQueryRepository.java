package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.PaymentLogRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffPaymentQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffPaymentQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PaymentLogRow> listPayments(String q, String status, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        StringBuilder sql = new StringBuilder("""
            SELECT TOP (:limit)
              p.id AS payment_id,
              o.id AS order_id,
              o.order_code,
              p.provider,
              p.provider_transaction_id,
              p.status,
              p.amount,
              p.created_at,
              p.updated_at,
              p.paid_at,
              p.expired_at
            FROM dbo.payments p
            JOIN dbo.[orders] o ON o.id = p.order_id
            WHERE o.deleted_at IS NULL
        """);

        MapSqlParameterSource ps = new MapSqlParameterSource().addValue("limit", safeLimit);

        if (q != null && !q.trim().isEmpty()) {
            sql.append(" AND (o.order_code LIKE :q OR p.provider_transaction_id LIKE :q) ");
            ps.addValue("q", "%" + q.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND p.status = :status ");
            ps.addValue("status", status.trim().toUpperCase());
        }

        sql.append(" ORDER BY COALESCE(p.updated_at, p.created_at) DESC ");

        return jdbc.query(sql.toString(), ps, (rs, n) -> {
            PaymentLogRow r = new PaymentLogRow();
            r.setPaymentId(rs.getLong("payment_id"));
            r.setOrderId(rs.getLong("order_id"));
            r.setOrderCode(rs.getString("order_code"));
            r.setProvider(rs.getString("provider"));
            r.setProviderTransactionId(rs.getString("provider_transaction_id"));
            r.setStatus(rs.getString("status"));
            r.setAmount(rs.getBigDecimal("amount"));

            if (rs.getTimestamp("created_at") != null) r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null) r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            if (rs.getTimestamp("paid_at") != null) r.setPaidAt(rs.getTimestamp("paid_at").toLocalDateTime());
            if (rs.getTimestamp("expired_at") != null) r.setExpiredAt(rs.getTimestamp("expired_at").toLocalDateTime());
            return r;
        });
    }

    public String getEventsJson(long paymentId) {
        String sql = "SELECT events_json FROM dbo.payments WHERE id = :id";
        MapSqlParameterSource ps = new MapSqlParameterSource("id", paymentId);
        List<String> rows = jdbc.query(sql, ps, (rs, n) -> rs.getString("events_json"));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public String getPaymentStatus(long paymentId) {
        String sql = "SELECT status FROM dbo.payments WHERE id = :id";
        MapSqlParameterSource ps = new MapSqlParameterSource("id", paymentId);
        List<String> rows = jdbc.query(sql, ps, (rs, n) -> rs.getString("status"));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public long getOrderIdByPaymentId(long paymentId) {
        String sql = "SELECT order_id FROM dbo.payments WHERE id = :id";
        MapSqlParameterSource ps = new MapSqlParameterSource("id", paymentId);
        List<Long> rows = jdbc.query(sql, ps, (rs, n) -> rs.getLong("order_id"));
        if (rows.isEmpty()) throw new RuntimeException("Payment not found: " + paymentId);
        return rows.get(0);
    }
}