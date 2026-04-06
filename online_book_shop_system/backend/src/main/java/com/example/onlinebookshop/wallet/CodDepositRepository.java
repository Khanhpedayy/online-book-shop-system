package com.example.onlinebookshop.wallet;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class CodDepositRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CodDepositRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ─── Insert ───────────────────────────────────────────────

    public Long insert(long staffId, BigDecimal amount, String depositCode,
                       Integer payosOrderCode, String payosLinkId, String checkoutUrl) {
        String sql = """
            INSERT INTO dbo.cod_deposits
                (deposit_code, staff_id, amount, status, payos_order_code, payos_link_id, checkout_url)
            OUTPUT INSERTED.id
            VALUES (:code, :staffId, :amount, 'PENDING', :payosCode, :linkId, :url)
            """;
        List<Long> ids = jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("code", depositCode)
                        .addValue("staffId", staffId)
                        .addValue("amount", amount)
                        .addValue("payosCode", payosOrderCode)
                        .addValue("linkId", payosLinkId)
                        .addValue("url", checkoutUrl),
                (rs, rn) -> rs.getLong(1));
        return ids.isEmpty() ? null : ids.get(0);
    }

    // ─── Query by staff ────────────────────────────────────────

    public List<CodDepositRow> findByStaff(long staffId) {
        String sql = """
            SELECT cd.id, cd.deposit_code, cd.staff_id, cd.amount, cd.status,
                   cd.checkout_url, cd.paid_at, cd.created_at,
                   u.full_name AS staff_name, u.email AS staff_email
            FROM dbo.cod_deposits cd
            JOIN dbo.users u ON u.id = cd.staff_id
            WHERE cd.staff_id = :staffId
              AND cd.deleted_at IS NULL
            ORDER BY cd.created_at DESC
            """;
        return jdbc.query(sql, new MapSqlParameterSource("staffId", staffId), this::map);
    }

    // ─── Query pending for staff (to restrict duplicate deposits) ─

    public Optional<CodDepositRow> findByCodeAndStaff(String code, long staffId) {
        String sql = """
            SELECT cd.id, cd.deposit_code, cd.staff_id, cd.amount, cd.status,
                   cd.checkout_url, cd.paid_at, cd.created_at,
                   u.full_name AS staff_name, u.email AS staff_email
            FROM dbo.cod_deposits cd
            JOIN dbo.users u ON u.id = cd.staff_id
            WHERE cd.deposit_code = :code AND cd.staff_id = :staffId
              AND cd.deleted_at IS NULL
            """;
        List<CodDepositRow> rows = jdbc.query(sql,
                new MapSqlParameterSource("code", code).addValue("staffId", staffId),
                this::map);
        return rows.stream().findFirst();
    }

    public boolean hasPendingDeposit(long staffId) {
        String sql = """
            SELECT COUNT(*) FROM dbo.cod_deposits
            WHERE staff_id = :staffId AND status = 'PENDING' AND deleted_at IS NULL
            """;
        Integer count = jdbc.queryForObject(sql, new MapSqlParameterSource("staffId", staffId), Integer.class);
        return count != null && count > 0;
    }

    // ─── Admin queries ─────────────────────────────────────────

    public List<CodDepositRow> findAll(String status) {
        boolean filterStatus = status != null && !status.isBlank();
        String sql = """
            SELECT cd.id, cd.deposit_code, cd.staff_id, cd.amount, cd.status,
                   cd.checkout_url, cd.paid_at, cd.created_at,
                   u.full_name AS staff_name, u.email AS staff_email
            FROM dbo.cod_deposits cd
            JOIN dbo.users u ON u.id = cd.staff_id
            WHERE cd.deleted_at IS NULL
            """ + (filterStatus ? " AND cd.status = :status " : "") + """
            ORDER BY cd.created_at DESC
            """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filterStatus && status != null) {
            params.addValue("status", status.trim().toUpperCase());
        }
        return jdbc.query(sql, params, this::map);
    }

    // ─── Find by payosLinkId (webhook) ─────────────────────────

    public Optional<CodDepositRow> findByPayosLinkId(String payosLinkId) {
        String sql = """
            SELECT cd.id, cd.deposit_code, cd.staff_id, cd.amount, cd.status,
                   cd.checkout_url, cd.paid_at, cd.created_at,
                   u.full_name AS staff_name, u.email AS staff_email
            FROM dbo.cod_deposits cd
            JOIN dbo.users u ON u.id = cd.staff_id
            WHERE cd.payos_link_id = :linkId
              AND cd.deleted_at IS NULL
            """;
        List<CodDepositRow> rows = jdbc.query(sql,
                new MapSqlParameterSource("linkId", payosLinkId), this::map);
        return rows.stream().findFirst();
    }

    public boolean existsByPayosLinkId(String payosLinkId) {
        String sql = """
            SELECT COUNT(*) FROM dbo.cod_deposits
            WHERE payos_link_id = :linkId AND deleted_at IS NULL
            """;
        Integer count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("linkId", payosLinkId), Integer.class);
        return count != null && count > 0;
    }

    // ─── Update status ─────────────────────────────────────────

    public int markPaid(long id) {
        String sql = """
            UPDATE dbo.cod_deposits
            SET status = 'PAID', paid_at = GETDATE(), updated_at = GETDATE()
            WHERE id = :id AND status = 'PENDING' AND deleted_at IS NULL
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    public int markCancelled(long id) {
        String sql = """
            UPDATE dbo.cod_deposits
            SET status = 'CANCELLED', updated_at = GETDATE()
            WHERE id = :id AND status = 'PENDING' AND deleted_at IS NULL
            """;
        return jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // ─── Row mapper ────────────────────────────────────────────

    private CodDepositRow map(java.sql.ResultSet rs, int rn) throws java.sql.SQLException {
        CodDepositRow r = new CodDepositRow();
        r.setId(rs.getLong("id"));
        r.setDepositCode(rs.getString("deposit_code"));
        r.setStaffId(rs.getLong("staff_id"));
        r.setAmount(rs.getBigDecimal("amount"));
        r.setStatus(rs.getString("status"));
        r.setCheckoutUrl(rs.getString("checkout_url"));
        r.setPaidAt(rs.getTimestamp("paid_at") == null ? null
                : rs.getTimestamp("paid_at").toLocalDateTime());
        r.setCreatedAt(rs.getTimestamp("created_at") == null ? null
                : rs.getTimestamp("created_at").toLocalDateTime());
        try { r.setStaffName(rs.getString("staff_name")); } catch (Exception ignored) {}
        try { r.setStaffEmail(rs.getString("staff_email")); } catch (Exception ignored) {}
        return r;
    }
}
