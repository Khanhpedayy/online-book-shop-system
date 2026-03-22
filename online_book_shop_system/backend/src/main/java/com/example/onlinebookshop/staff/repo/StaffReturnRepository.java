package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.ReturnIntakeView;
import com.example.onlinebookshop.staff.dto.ReturnItemRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffReturnRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffReturnRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ReturnIntakeView getCreateScreenOrderInfo(long orderId) {
        String sql = """
            SELECT TOP 1
              o.id AS order_id, o.order_code, o.ship_name, o.ship_phone
            FROM dbo.orders o
            WHERE o.deleted_at IS NULL AND o.id = :oid
            """;
        List<ReturnIntakeView> rows = jdbc.query(sql, new MapSqlParameterSource("oid", orderId), (rs, n) -> {
            ReturnIntakeView v = new ReturnIntakeView();
            v.setOrderId(rs.getLong("order_id"));
            v.setOrderCode(rs.getString("order_code"));
            v.setShipName(rs.getString("ship_name"));
            v.setShipPhone(rs.getString("ship_phone"));
            return v;
        });
        if (rows.isEmpty()) throw new RuntimeException("Order not found: " + orderId);
        return rows.get(0);
    }

    public long insertReturn(long orderId, String returnCode, String reason, String note) {
        String sql = """
            INSERT INTO dbo.returns(return_code, order_id, status, reason, note, received_at)
            VALUES (:code, :oid, 'RECEIVED', :reason, :note, SYSUTCDATETIME())
            """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("code", returnCode)
                .addValue("oid", orderId)
                .addValue("reason", reason)
                .addValue("note", note), kh, new String[]{"id"});
        Number key = kh.getKey();
        if (key == null) throw new RuntimeException("Cannot create return");
        return key.longValue();
    }

    public ReturnIntakeView getReturnHeader(long returnId) {
        String sql = """
            SELECT TOP 1
              r.id AS return_id, r.return_code, r.status, r.reason, r.note, r.received_at,
              o.id AS order_id, o.order_code, o.ship_name, o.ship_phone
            FROM dbo.returns r
            JOIN dbo.orders o ON o.id = r.order_id
            WHERE r.deleted_at IS NULL AND o.deleted_at IS NULL
              AND r.id = :rid
            """;
        List<ReturnIntakeView> rows = jdbc.query(sql, new MapSqlParameterSource("rid", returnId), (rs, n) -> {
            ReturnIntakeView v = new ReturnIntakeView();
            v.setReturnId(rs.getLong("return_id"));
            v.setReturnCode(rs.getString("return_code"));
            v.setStatus(rs.getString("status"));
            v.setReason(rs.getString("reason"));
            v.setNote(rs.getString("note"));

            Timestamp t = rs.getTimestamp("received_at");
            v.setReceivedAt(t == null ? null : t.toLocalDateTime());

            v.setOrderId(rs.getLong("order_id"));
            v.setOrderCode(rs.getString("order_code"));
            v.setShipName(rs.getString("ship_name"));
            v.setShipPhone(rs.getString("ship_phone"));
            return v;
        });
        if (rows.isEmpty()) throw new RuntimeException("Return not found: " + returnId);
        return rows.get(0);
    }

    public Optional<Long> findCopyIdByCode(String copyCode) {
        String sql = """
            SELECT TOP 1 c.id
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL AND c.copy_code = :code
            """;
        List<Long> rows = jdbc.queryForList(sql, new MapSqlParameterSource("code", copyCode), Long.class);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Validate orderItem thuộc order của return, và lấy snapshot để hiển thị. */
    public OrderItemForReturn getOrderItemForReturn(long returnId, long orderItemId) {
        String sql = """
            SELECT TOP 1
              oi.id AS order_item_id,
              oi.order_id,
              oi.variant_id,
              oi.copy_id AS allocated_copy_id,
              oi.sku_snapshot,
              oi.title_snapshot
            FROM dbo.returns r
            JOIN dbo.orders o ON o.id = r.order_id AND o.deleted_at IS NULL
            JOIN dbo.order_items oi ON oi.order_id = o.id AND oi.deleted_at IS NULL
            WHERE r.deleted_at IS NULL
              AND r.id = :rid
              AND oi.id = :oiid
            """;
        List<OrderItemForReturn> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("rid", returnId)
                .addValue("oiid", orderItemId), (rs, n) ->
                new OrderItemForReturn(
                        rs.getLong("order_item_id"),
                        rs.getLong("order_id"),
                        rs.getLong("variant_id"),
                        (rs.getObject("allocated_copy_id") == null ? null : rs.getLong("allocated_copy_id")),
                        rs.getString("sku_snapshot"),
                        rs.getString("title_snapshot")
                )
        );
        if (rows.isEmpty()) throw new RuntimeException("Order item không thuộc order của return.");
        return rows.get(0);
    }

    public boolean existsReturnItemByCopy(long returnId, long copyId) {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.return_items ri
            WHERE ri.return_id = :rid AND ri.copy_id = :cid
            """;
        Long c = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("rid", returnId)
                .addValue("cid", copyId), Long.class);
        return c != null && c > 0;
    }

    public long insertReturnItem(long returnId,
                                 long orderItemId,
                                 long copyId,
                                 String receivedConditionGrade,
                                 String receivedConditionNote) {
        String sql = """
            INSERT INTO dbo.return_items(return_id, order_item_id, copy_id, quantity,
                                         received_condition_grade, received_condition_note)
            VALUES (:rid, :oiid, :cid, 1, :g, :n)
            """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("rid", returnId)
                .addValue("oiid", orderItemId)
                .addValue("cid", copyId)
                .addValue("g", receivedConditionGrade)
                .addValue("n", receivedConditionNote), kh, new String[]{"id"});
        Number key = kh.getKey();
        if (key == null) throw new RuntimeException("Cannot insert return item");
        return key.longValue();
    }

    public List<ReturnItemRow> getReturnItems(long returnId) {
        String sql = """
            SELECT
              ri.id, ri.order_item_id, ri.copy_id, ri.received_condition_grade, ri.received_condition_note,
              ri.action, ri.created_at,
              c.copy_code,
              oi.sku_snapshot, oi.title_snapshot
            FROM dbo.return_items ri
            LEFT JOIN dbo.copies c ON c.id = ri.copy_id
            JOIN dbo.order_items oi ON oi.id = ri.order_item_id
            WHERE ri.return_id = :rid
            ORDER BY ri.id DESC
            """;
        return jdbc.query(sql, new MapSqlParameterSource("rid", returnId), (rs, n) -> {
            ReturnItemRow r = new ReturnItemRow();
            r.setId(rs.getLong("id"));
            r.setOrderItemId(rs.getLong("order_item_id"));
            r.setCopyId(rs.getObject("copy_id") == null ? null : rs.getLong("copy_id"));
            r.setCopyCode(rs.getString("copy_code"));
            r.setSkuSnapshot(rs.getString("sku_snapshot"));
            r.setTitleSnapshot(rs.getString("title_snapshot"));
            r.setReceivedConditionGrade(rs.getString("received_condition_grade"));
            r.setReceivedConditionNote(rs.getString("received_condition_note"));
            r.setAction(rs.getString("action"));
            Timestamp t = rs.getTimestamp("created_at");
            r.setCreatedAt(t == null ? null : t.toLocalDateTime());
            return r;
        });
    }

    public record OrderItemForReturn(long orderItemId,
                                     long orderId,
                                     long variantId,
                                     Long allocatedCopyId,
                                     String skuSnapshot,
                                     String titleSnapshot) {}
}