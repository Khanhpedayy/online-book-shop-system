package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.StaffAlert;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StaffAlertRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffAlertRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Alerts:
     * 1) OVERDUE: order NEW/CONFIRMED quá 24h
     * 2) PAYMENT_MISMATCH: payments SUCCEEDED nhưng orders.payment_status != PAID
     * 3) STOCK_ISSUE: order_items chưa allocate (copy_id null) và không còn AVAILABLE copy cho variant
     */
    public List<StaffAlert> getAlerts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<StaffAlert> out = new ArrayList<>();

        out.addAll(overdueAlerts(Math.min(safeLimit, 20)));
        out.addAll(paymentMismatchAlerts(Math.min(safeLimit, 20)));
        out.addAll(stockIssueAlerts(Math.min(safeLimit, 20)));

        // cap tổng
        if (out.size() > safeLimit) return out.subList(0, safeLimit);
        return out;
    }

    private List<StaffAlert> overdueAlerts(int limit) {
        String sql = """
            SELECT TOP (:limit)
              o.id, o.order_code, o.placed_at
            FROM dbo.[orders] o
            WHERE o.deleted_at IS NULL
              AND o.status IN ('NEW','CONFIRMED')
              AND o.placed_at < :threshold
            ORDER BY o.placed_at ASC
            """;

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("threshold", threshold);

        return jdbc.query(sql, p, (rs, n) -> {
            StaffAlert a = new StaffAlert();
            a.setSeverity("WARN");
            a.setType("OVERDUE");
            a.setOrderId(rs.getLong("id"));
            a.setOrderCode(rs.getString("order_code"));
            a.setCreatedAt(rs.getTimestamp("placed_at").toLocalDateTime());
            a.setMessage("Đơn quá 24h chưa xử lý (NEW/CONFIRMED).");
            return a;
        });
    }

    private List<StaffAlert> paymentMismatchAlerts(int limit) {
        String sql = """
            SELECT TOP (:limit)
              o.id, o.order_code, p.updated_at
            FROM dbo.payments p
            JOIN dbo.[orders] o ON o.id = p.order_id
            WHERE o.deleted_at IS NULL
              AND p.status = 'SUCCEEDED'
              AND o.payment_status <> 'PAID'
            ORDER BY COALESCE(p.updated_at, p.created_at) DESC
            """;

        MapSqlParameterSource p = new MapSqlParameterSource().addValue("limit", limit);

        return jdbc.query(sql, p, (rs, n) -> {
            StaffAlert a = new StaffAlert();
            a.setSeverity("CRITICAL");
            a.setType("PAYMENT_MISMATCH");
            a.setOrderId(rs.getLong("id"));
            a.setOrderCode(rs.getString("order_code"));
            if (rs.getTimestamp("updated_at") != null) {
                a.setCreatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            a.setMessage("Payment SUCCEEDED nhưng order chưa set PAID.");
            return a;
        });
    }

    private List<StaffAlert> stockIssueAlerts(int limit) {
        String sql = """
            SELECT TOP (:limit)
              o.id, o.order_code, o.placed_at
            FROM dbo.order_items oi
            JOIN dbo.[orders] o ON o.id = oi.order_id
            WHERE o.deleted_at IS NULL
              AND oi.deleted_at IS NULL
              AND o.status IN ('NEW','CONFIRMED')
              AND oi.copy_id IS NULL
              AND NOT EXISTS (
                SELECT 1 FROM dbo.copies c
                WHERE c.deleted_at IS NULL
                  AND c.status = 'AVAILABLE'
                  AND c.variant_id = oi.variant_id
              )
            ORDER BY o.placed_at ASC
            """;

        MapSqlParameterSource p = new MapSqlParameterSource().addValue("limit", limit);

        return jdbc.query(sql, p, (rs, n) -> {
            StaffAlert a = new StaffAlert();
            a.setSeverity("WARN");
            a.setType("STOCK_ISSUE");
            a.setOrderId(rs.getLong("id"));
            a.setOrderCode(rs.getString("order_code"));
            a.setCreatedAt(rs.getTimestamp("placed_at").toLocalDateTime());
            a.setMessage("Có item chưa allocate nhưng kho không còn bản AVAILABLE.");
            return a;
        });
    }
}