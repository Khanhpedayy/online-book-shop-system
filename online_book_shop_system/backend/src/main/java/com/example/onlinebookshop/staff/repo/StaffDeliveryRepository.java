package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffDeliveryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffDeliveryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public OrderHeader getOrderHeader(long orderId) {
        String sql = """
            SELECT TOP 1 o.id, o.order_code, o.status, o.payment_status,
                         o.carrier, o.tracking_code,
                         o.staff_note
            FROM dbo.orders o
            WHERE o.deleted_at IS NULL AND o.id = :id
            """;
        List<OrderHeader> rows = jdbc.query(sql, new MapSqlParameterSource("id", orderId), (rs, n) ->
                new OrderHeader(
                        rs.getLong("id"),
                        rs.getString("order_code"),
                        rs.getString("status"),
                        rs.getString("payment_status"),
                        rs.getString("carrier"),
                        rs.getString("tracking_code"),
                        rs.getString("staff_note")
                )
        );
        if (rows.isEmpty()) throw new RuntimeException("Order not found: " + orderId);
        return rows.get(0);
    }

    /** Outcome = DELIVERED: set status=DELIVERED + delivered_at */
    public int markDelivered(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'DELIVERED',
                delivered_at = COALESCE(delivered_at, SYSUTCDATETIME())
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('SHIPPED','DELIVERED')
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", orderId));
    }

    /**
     * Outcome = FAILED:
     * - KHÔNG có status FAILED trong schema -> giữ status SHIPPED
     * - append staff_note: [DELIVERY_FAILED] reason | at=...
     */
    public int markDeliveryFailed(long orderId, String reason) {
        String sql = """
            UPDATE dbo.orders
            SET staff_note =
                CASE
                  WHEN staff_note IS NULL OR LTRIM(RTRIM(staff_note)) = '' THEN
                    CONCAT('[DELIVERY_FAILED] ', :reason, ' | at=', CONVERT(varchar(19), SYSUTCDATETIME(), 120))
                  ELSE
                    CONCAT(staff_note, CHAR(10),
                           '[DELIVERY_FAILED] ', :reason, ' | at=', CONVERT(varchar(19), SYSUTCDATETIME(), 120))
                END
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('SHIPPED','DELIVERED')
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderId)
                .addValue("reason", reason));
    }

    /**
     * Auto complete: nếu status=DELIVERED và payment_status=PAID -> COMPLETED + completed_at
     * (schema có completed_at)
     */
    public int autoCompleteIfEligible(long orderId) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'COMPLETED',
                completed_at = COALESCE(completed_at, SYSUTCDATETIME())
            WHERE id = :id
              AND deleted_at IS NULL
              AND status = 'DELIVERED'
              AND payment_status = 'PAID'
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", orderId));
    }

    public record OrderHeader(
            long id,
            String orderCode,
            String status,
            String paymentStatus,
            String carrier,
            String trackingCode,
            String staffNote
    ) {}
}