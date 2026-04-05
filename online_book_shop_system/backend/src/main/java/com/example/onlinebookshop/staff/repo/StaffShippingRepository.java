package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.ShippingItemRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffShippingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffShippingRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public OrderHeader getOrderHeader(long orderId) {
        String sql = """
            SELECT TOP 1 o.id, o.order_code, o.status, o.payment_status,
                         o.user_id,
                         o.ship_name, o.ship_phone,
                         o.ship_line1, o.ship_line2, o.ship_ward, o.ship_district, o.ship_city, o.ship_province,
                         o.carrier, o.tracking_code
            FROM dbo.orders o
            WHERE o.deleted_at IS NULL AND o.id = :id
            """;
        List<OrderHeader> rows = jdbc.query(sql, new MapSqlParameterSource("id", orderId), (rs, n) ->
                new OrderHeader(
                        rs.getLong("id"),
                        rs.getString("order_code"),
                        rs.getString("status"),
                        rs.getString("payment_status"),
                        rs.getLong("user_id"),
                        rs.getString("ship_name"),
                        rs.getString("ship_phone"),
                        rs.getString("ship_line1"),
                        rs.getString("ship_line2"),
                        rs.getString("ship_ward"),
                        rs.getString("ship_district"),
                        rs.getString("ship_city"),
                        rs.getString("ship_province"),
                        rs.getString("carrier"),
                        rs.getString("tracking_code")
                )
        );
        if (rows.isEmpty()) throw new RuntimeException("Order not found: " + orderId);
        return rows.get(0);
    }

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

    /**
     * Ưu tiên dữ liệu fulfillment thực tế:
     * - có copy_id
     * - có copy_code / location
     */
    public List<ShippingItemRow> getItemsForSlip(long orderId) {
        String sql = """
            SELECT
              oi.id AS order_item_id,
              oi.sku_snapshot,
              oi.title_snapshot,
              oi.copy_id,
              c.copy_code,
              c.location
            FROM dbo.order_items oi
            LEFT JOIN dbo.copies c ON c.id = oi.copy_id AND c.deleted_at IS NULL
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.quantity = 1
            ORDER BY c.location ASC, c.copy_code ASC, oi.id ASC
            """;
        return jdbc.query(sql, new MapSqlParameterSource("oid", orderId), (rs, n) -> {
            ShippingItemRow r = new ShippingItemRow();
            r.setOrderItemId(rs.getLong("order_item_id"));
            r.setSkuSnapshot(rs.getString("sku_snapshot"));
            r.setTitleSnapshot(rs.getString("title_snapshot"));

            Object cid = rs.getObject("copy_id");
            r.setCopyId(cid == null ? null : ((Number) cid).longValue());

            r.setCopyCode(rs.getString("copy_code"));
            r.setLocation(rs.getString("location"));
            return r;
        });
    }

    /**
     * Fallback khi chưa allocate/pick thật:
     * lấy từ order_items để packing slip không bị trắng.
     */
    public List<ShippingItemRow> getFallbackItemsForSlip(long orderId) {
        String sql = """
            SELECT
                oi.id AS order_item_id,
                oi.sku_snapshot,
                oi.title_snapshot,
                oi.quantity
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
            ORDER BY oi.id ASC
            """;

        return jdbc.query(sql, new MapSqlParameterSource("oid", orderId), (rs, n) -> {
            ShippingItemRow r = new ShippingItemRow();
            r.setOrderItemId(rs.getLong("order_item_id"));
            r.setSkuSnapshot(rs.getString("sku_snapshot"));
            r.setTitleSnapshot(rs.getString("title_snapshot"));

            // chưa có copy thật thì để placeholder
            r.setCopyId(null);

            Integer qty = rs.getObject("quantity", Integer.class);
            if (qty != null && qty > 1) {
                r.setCopyCode("x" + qty);
            } else {
                r.setCopyCode("-");
            }

            r.setLocation("-");
            return r;
        });
    }

    public int markShipped(long orderId, String carrier, String trackingCode) {
        String sql = """
            UPDATE dbo.orders
            SET status = 'SHIPPED',
                shipped_at = SYSUTCDATETIME(),
                carrier = :carrier,
                tracking_code = :tracking
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('PACKED','SHIPPED')
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderId)
                .addValue("carrier", carrier)
                .addValue("tracking", trackingCode));
    }

    public record OrderHeader(
            long id,
            String orderCode,
            String status,
            String paymentStatus,
            long userId,
            String shipName,
            String shipPhone,
            String shipLine1,
            String shipLine2,
            String shipWard,
            String shipDistrict,
            String shipCity,
            String shipProvince,
            String carrier,
            String trackingCode
    ) {}
}