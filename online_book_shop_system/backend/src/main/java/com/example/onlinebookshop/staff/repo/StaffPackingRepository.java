package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffPackingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffPackingRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public OrderHeader getOrderHeader(long orderId) {
        String sql = """
            SELECT TOP 1 o.id, o.order_code, o.status, o.payment_status,
                         o.ship_name, o.ship_phone,
                         o.ship_line1, o.ship_line2, o.ship_ward, o.ship_district, o.ship_city, o.ship_province,
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
                        rs.getString("ship_name"),
                        rs.getString("ship_phone"),
                        rs.getString("ship_line1"),
                        rs.getString("ship_line2"),
                        rs.getString("ship_ward"),
                        rs.getString("ship_district"),
                        rs.getString("ship_city"),
                        rs.getString("ship_province"),
                        rs.getString("carrier"),
                        rs.getString("tracking_code"),
                        rs.getString("staff_note")
                )
        );
        if (rows.isEmpty()) throw new RuntimeException("Order not found: " + orderId);
        return rows.get(0);
    }

    /** số item đã allocate (order_items.copy_id != null) */
    public int countAllocated(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id IS NOT NULL
              AND oi.quantity = 1
            """;
        Integer c = jdbc.queryForObject(sql, new MapSqlParameterSource("oid", orderId), Integer.class);
        return c == null ? 0 : c;
    }

    /** số item đã picked (picked_at != null) */
    public int countPicked(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id IS NOT NULL
              AND oi.quantity = 1
              AND oi.picked_at IS NOT NULL
            """;
        Integer c = jdbc.queryForObject(sql, new MapSqlParameterSource("oid", orderId), Integer.class);
        return c == null ? 0 : c;
    }

    /** total per-copy item cần pack (quantity=1) */
    public int countPackableItems(long orderId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.quantity = 1
            """;
        Integer c = jdbc.queryForObject(sql, new MapSqlParameterSource("oid", orderId), Integer.class);
        return c == null ? 0 : c;
    }

    /** Update trạng thái PACKED + packed_at + append staff_note */
    public int markPacked(long orderId, int boxCount, String packingNote) {
        // append staff_note: thêm 1 dòng [PACK] ...
        String sql = """
            UPDATE dbo.orders
            SET status = 'PACKED',
                packed_at = SYSUTCDATETIME(),
                staff_note =
                    CASE
                      WHEN staff_note IS NULL OR LTRIM(RTRIM(staff_note)) = '' THEN
                        CONCAT('[PACK] boxes=', :boxes, ' | ', COALESCE(:note, ''), ' | at=', CONVERT(varchar(19), SYSUTCDATETIME(), 120))
                      ELSE
                        CONCAT(staff_note, CHAR(10),
                               '[PACK] boxes=', :boxes, ' | ', COALESCE(:note, ''), ' | at=', CONVERT(varchar(19), SYSUTCDATETIME(), 120))
                    END
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('CONFIRMED','PACKED')
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderId)
                .addValue("boxes", boxCount)
                .addValue("note", packingNote));
    }

    public record OrderHeader(
            long id,
            String orderCode,
            String status,
            String paymentStatus,
            String shipName,
            String shipPhone,
            String shipLine1,
            String shipLine2,
            String shipWard,
            String shipDistrict,
            String shipCity,
            String shipProvince,
            String carrier,
            String trackingCode,
            String staffNote
    ) {}
}