package com.example.onlinebookshop.staffdashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffDashboardRepository {

    private final JdbcTemplate jdbc;

    public StaffDashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StaffKpiDTO getKpis() {
        StaffKpiDTO kpi = new StaffKpiDTO();
        kpi.setNewOrders(jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status = 'NEW'", Integer.class));
        kpi.setPendingPayments(jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE payment_status = 'PENDING'", Integer.class));
        kpi.setOrdersToPack(jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status = 'CONFIRMED' AND packed_at IS NULL", Integer.class));
        kpi.setShippedToday(jdbc.queryForObject(
                "SELECT COUNT(*) FROM shipments WHERE CAST(shipped_at AS DATE) = CAST(SYSUTCDATETIME() AS DATE)",
                Integer.class));
        return kpi;
    }

    public List<TodoItemDTO> getTodoList() {
        String sql = "SELECT o.id, o.order_code, o.status, u.full_name AS customer_name, "
                + "o.priority, o.sla_deadline, o.placed_at, "
                + "CASE "
                + "  WHEN o.status = 'NEW' THEN 'CONFIRM' "
                + "  WHEN o.status = 'CONFIRMED' AND o.packed_at IS NULL THEN 'PICK' "
                + "  WHEN o.packed_at IS NOT NULL AND o.delivery_status = 'NOT_SHIPPED' THEN 'SHIP' "
                + "  ELSE 'REVIEW' "
                + "END AS action "
                + "FROM orders o LEFT JOIN users u ON o.user_id = u.id "
                + "WHERE o.status IN ('NEW','CONFIRMED') "
                + "OR (o.packed_at IS NOT NULL AND o.delivery_status = 'NOT_SHIPPED') "
                + "ORDER BY o.priority DESC, o.sla_deadline ASC, o.created_at ASC";
        return jdbc.query(sql, (rs, i) -> {
            TodoItemDTO d = new TodoItemDTO();
            d.setOrderId(rs.getLong("id"));
            d.setOrderCode(rs.getString("order_code"));
            d.setStatus(rs.getString("status"));
            d.setCustomerName(rs.getString("customer_name"));
            d.setPriority(rs.getInt("priority"));
            d.setAction(rs.getString("action"));
            if (rs.getTimestamp("sla_deadline") != null)
                d.setSlaDeadline(rs.getTimestamp("sla_deadline").toLocalDateTime().toString());
            if (rs.getTimestamp("placed_at") != null)
                d.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime().toString());
            return d;
        });
    }

    public List<AlertDTO> getAlerts() {
        String sql =
                // Overdue orders (placed > 48h ago, not shipped)
                "SELECT 'OVERDUE_ORDER' AS type, 'HIGH' AS severity, "
                        + "'Order ' + o.order_code + ' is overdue (placed ' + CONVERT(VARCHAR, DATEDIFF(HOUR, o.placed_at, SYSUTCDATETIME())) + 'h ago)' AS message, "
                        + "o.id AS reference_id, o.order_code AS reference_code, o.placed_at AS created_at "
                        + "FROM orders o WHERE o.status NOT IN ('COMPLETED','CANCELLED') "
                        + "AND o.delivery_status != 'DELIVERED' AND DATEDIFF(HOUR, o.placed_at, SYSUTCDATETIME()) > 48 "
                        + "UNION ALL "
                        // Payment mismatches
                        + "SELECT 'PAYMENT_MISMATCH' AS type, 'HIGH' AS severity, "
                        + "'Payment mismatch for order ' + o.order_code AS message, "
                        + "o.id AS reference_id, o.order_code AS reference_code, pl.created_at "
                        + "FROM payment_logs pl JOIN orders o ON pl.order_id = o.id WHERE pl.flagged = 1 "
                        + "UNION ALL "
                        // Low stock (variants with total available < 5)
                        + "SELECT 'LOW_STOCK' AS type, 'MEDIUM' AS severity, "
                        + "'Variant ' + v.sku + ' (' + b.title + ') has only ' + CONVERT(VARCHAR, SUM(l.qty_available)) + ' left' AS message, "
                        + "v.id AS reference_id, v.sku AS reference_code, SYSUTCDATETIME() AS created_at "
                        + "FROM book_variants v JOIN books b ON v.book_id = b.id "
                        + "JOIN lots l ON l.variant_id = v.id AND l.status = 'RELEASED' "
                        + "GROUP BY v.id, v.sku, b.title HAVING SUM(l.qty_available) < 5 "
                        + "ORDER BY created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            AlertDTO d = new AlertDTO();
            d.setType(rs.getString("type"));
            d.setSeverity(rs.getString("severity"));
            d.setMessage(rs.getString("message"));
            d.setReferenceId(rs.getLong("reference_id"));
            d.setReferenceCode(rs.getString("reference_code"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        });
    }
}

