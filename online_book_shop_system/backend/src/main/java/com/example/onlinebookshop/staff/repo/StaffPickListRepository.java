package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.PickListItemRow;
import com.example.onlinebookshop.staff.dto.PickListView;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffPickListRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffPickListRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PickListView getOrderHeader(long orderId) {
        String sql = """
            SELECT TOP 1 o.id, o.order_code, o.status
            FROM dbo.[orders] o
            WHERE o.deleted_at IS NULL AND o.id = :id
            """;
        List<PickListView> rows = jdbc.query(sql, new MapSqlParameterSource("id", orderId), (rs, n) -> {
            PickListView v = new PickListView();
            v.setOrderId(rs.getLong("id"));
            v.setOrderCode(rs.getString("order_code"));
            v.setStatus(rs.getString("status"));
            return v;
        });
        if (rows.isEmpty()) throw new RuntimeException("Order not found: " + orderId);
        return rows.get(0);
    }

    /** Pick list: chỉ lấy order_items đã allocate copy_id */
    public List<PickListItemRow> getPickListItems(long orderId) {
        String sql = """
            SELECT
              oi.id AS order_item_id,
              oi.sku_snapshot,
              oi.title_snapshot,
              oi.copy_id,
              oi.pick_method,
              oi.picked_at,
              c.copy_code,
              c.location,
              c.status AS copy_status
            FROM dbo.order_items oi
            JOIN dbo.copies c ON c.id = oi.copy_id AND c.deleted_at IS NULL
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id IS NOT NULL
              AND oi.quantity = 1
            ORDER BY
              c.location ASC,
              c.copy_code ASC,
              oi.id ASC
            """;

        return jdbc.query(sql, new MapSqlParameterSource("oid", orderId), (rs, n) -> {
            PickListItemRow r = new PickListItemRow();
            r.setOrderItemId(rs.getLong("order_item_id"));
            r.setSkuSnapshot(rs.getString("sku_snapshot"));
            r.setTitleSnapshot(rs.getString("title_snapshot"));
            Object cid = rs.getObject("copy_id");
            r.setCopyId(cid == null ? null : ((Number) cid).longValue());
            r.setCopyCode(rs.getString("copy_code"));
            r.setLocation(rs.getString("location"));
            r.setCopyStatus(rs.getString("copy_status"));
            r.setPickMethod(rs.getString("pick_method"));
            Timestamp t = rs.getTimestamp("picked_at");
            r.setPickedAt(t == null ? null : t.toLocalDateTime());
            return r;
        });
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

    public Optional<Long> findCopyIdByCode(String copyCode) {
        String sql = """
            SELECT TOP 1 c.id
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL AND c.copy_code = :code
            """;
        List<Long> ids = jdbc.queryForList(sql, new MapSqlParameterSource("code", copyCode), Long.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    /** Validate: copy này có thuộc order không? -> lấy order_item_id */
    public Optional<Long> findOrderItemIdByOrderAndCopy(long orderId, long copyId) {
        String sql = """
            SELECT TOP 1 oi.id
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id = :cid
              AND oi.quantity = 1
            """;
        List<Long> ids = jdbc.queryForList(sql, new MapSqlParameterSource()
                .addValue("oid", orderId)
                .addValue("cid", copyId), Long.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    /** Mark picked (idempotent nhẹ): chỉ set picked_at nếu chưa picked */
    public int markOrderItemPicked(long orderItemId, String method) {
        String sql = """
            UPDATE dbo.order_items
            SET picked_at = SYSUTCDATETIME(),
                pick_method = :m
            WHERE id = :id
              AND deleted_at IS NULL
              AND picked_at IS NULL
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderItemId)
                .addValue("m", method));
    }

    /** Update copy status -> PICKED (chỉ khi đang RESERVED/AVAILABLE) */
    public int markCopyPicked(long copyId) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'PICKED'
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('RESERVED','AVAILABLE')
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", copyId));
    }
}