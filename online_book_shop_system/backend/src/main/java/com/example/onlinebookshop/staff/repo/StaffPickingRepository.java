package com.example.onlinebookshop.staff.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Picking/Allocation repo (JDBC) cho nghiệp vụ staff:
 * - Auto allocate FIFO (copy AVAILABLE -> RESERVED, gán vào order_items.copy_id)
 * - Manual pick theo copy_code (scan)
 * - Unpick (trả copy về AVAILABLE)
 *
 * NOTE: Đây là version "đủ dùng để demo/nộp" (không cover mọi edge-case phức tạp).
 */
@Repository
public class StaffPickingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffPickingRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lấy danh sách order_item cần allocate (copy_id is null). */
    public List<Long> findUnallocatedOrderItemIds(long orderId) {
        String sql = """
            SELECT oi.id
            FROM dbo.order_items oi
            WHERE oi.deleted_at IS NULL
              AND oi.order_id = :oid
              AND oi.copy_id IS NULL
              AND oi.quantity = 1
            ORDER BY oi.id ASC
            """;

        return jdbc.queryForList(sql, new MapSqlParameterSource("oid", orderId), Long.class);
    }

    /** Lấy variant_id của order_item. */
    public long getVariantIdByOrderItemId(long orderItemId) {
        String sql = """
            SELECT oi.variant_id
            FROM dbo.order_items oi
            WHERE oi.id = :id AND oi.deleted_at IS NULL
            """;
        Long v = jdbc.queryForObject(sql, new MapSqlParameterSource("id", orderItemId), Long.class);
        if (v == null) throw new IllegalArgumentException("Order item not found: " + orderItemId);
        return v;
    }

    /** Chọn 1 copy AVAILABLE theo FIFO: ưu tiên created_at cũ hơn trước. */
    public Optional<Long> findFifoAvailableCopyId(long variantId) {
        String sql = """
            SELECT TOP 1 c.id
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL
              AND c.variant_id = :vid
              AND c.status = 'AVAILABLE'
            ORDER BY c.created_at ASC, c.id ASC
            """;
        List<Long> ids = jdbc.queryForList(sql, new MapSqlParameterSource("vid", variantId), Long.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    /** Tìm copy theo copy_code. */
    public Optional<CopyInfo> findCopyByCode(String copyCode) {
        String sql = """
            SELECT TOP 1 c.id, c.variant_id, c.status
            FROM dbo.copies c
            WHERE c.deleted_at IS NULL
              AND c.copy_code = :code
            """;
        List<CopyInfo> rows = jdbc.query(sql, new MapSqlParameterSource("code", copyCode), (rs, n) ->
                new CopyInfo(rs.getLong("id"), rs.getLong("variant_id"), rs.getString("status"))
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Đánh dấu copy RESERVED (chỉ thành công nếu đang AVAILABLE). */
    public int reserveCopy(long copyId, int ttlMinutes) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'RESERVED',
                reserved_at = SYSUTCDATETIME(),
                reserve_expires_at = DATEADD(minute, :ttl, SYSUTCDATETIME())
            WHERE id = :id
              AND deleted_at IS NULL
              AND status = 'AVAILABLE'
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", copyId)
                .addValue("ttl", ttlMinutes));
    }

    /** Trả copy về AVAILABLE (unpick). */
    public int releaseCopy(long copyId) {
        String sql = """
            UPDATE dbo.copies
            SET status = 'AVAILABLE',
                reserved_at = NULL,
                reserve_expires_at = NULL
            WHERE id = :id
              AND deleted_at IS NULL
              AND status IN ('RESERVED','PICKED')
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", copyId));
    }

    /** Gán copy_id vào order_item (chỉ khi order_item chưa có copy). */
    public int assignCopyToOrderItem(long orderItemId, long copyId, String pickMethod) {
        String sql = """
            UPDATE dbo.order_items
            SET copy_id = :cid,
                pick_method = :pm,
                picked_at = SYSUTCDATETIME()
            WHERE id = :id
              AND deleted_at IS NULL
              AND copy_id IS NULL
              AND quantity = 1
            """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", orderItemId)
                .addValue("cid", copyId)
                .addValue("pm", pickMethod));
    }

    /** Bỏ gán copy khỏi order_item. */
    public int unassignCopyFromOrderItem(long orderItemId) {
        String sql = """
            UPDATE dbo.order_items
            SET copy_id = NULL,
                pick_method = NULL,
                picked_at = NULL
            WHERE id = :id
              AND deleted_at IS NULL
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", orderItemId));
    }

    /** Lấy copy_id đang gán cho order_item (nếu có). */
    public Optional<Long> getAssignedCopyId(long orderItemId) {
        String sql = """
            SELECT oi.copy_id
            FROM dbo.order_items oi
            WHERE oi.id = :id AND oi.deleted_at IS NULL
            """;
        List<Long> rows = jdbc.query(sql, new MapSqlParameterSource("id", orderItemId), (rs, n) -> {
            Object v = rs.getObject("copy_id");
            return v == null ? null : rs.getLong("copy_id");
        });
        if (rows.isEmpty() || rows.get(0) == null) return Optional.empty();
        return Optional.of(rows.get(0));
    }

    /** Ghi inventory_transactions để demo audit trail. */
    public void insertInventoryTxReserve(long variantId, long copyId, long orderId, String note) {
        String sql = """
            INSERT INTO dbo.inventory_transactions(movement_type, variant_id, copy_id, quantity,
                                                  reference_type, reference_id, reason, note)
            VALUES ('RESERVE', :vid, :cid, 1, 'ORDER', :oid, 'SALE', :note)
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("vid", variantId)
                .addValue("cid", copyId)
                .addValue("oid", orderId)
                .addValue("note", note));
    }

    public record CopyInfo(long id, long variantId, String status) {}
}