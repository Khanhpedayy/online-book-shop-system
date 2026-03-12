package com.example.onlinebookshop.adjustment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ManagerAdjustmentRepository {

    private final JdbcTemplate jdbc;

    public ManagerAdjustmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AdjustmentDTO> findAll() {
        String sql = "SELECT it.id, it.movement_type, it.variant_id, "
                + "v.sku AS variant_sku, b.title AS book_title, "
                + "it.lot_id, l.lot_code, it.copy_id, c.copy_code, "
                + "it.quantity, it.from_location, it.to_location, "
                + "it.reference_type, it.reference_id, it.reason, it.note, it.created_at, "
                + "u.full_name AS created_by_name "
                + "FROM inventory_transactions it "
                + "LEFT JOIN book_variants v ON it.variant_id = v.id "
                + "LEFT JOIN books b ON v.book_id = b.id "
                + "LEFT JOIN lots l ON it.lot_id = l.id "
                + "LEFT JOIN copies c ON it.copy_id = c.id "
                + "LEFT JOIN users u ON it.created_by = u.id "
                + "WHERE it.movement_type = 'ADJUST' "
                + "ORDER BY it.created_at DESC";
        return jdbc.query(sql, (rs, i) -> mapDTO(rs));
    }

    public Long insert(CreateAdjustmentRequest req) {
        String sql = "INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, copy_id, "
                + "quantity, reference_type, reason, note) VALUES ('ADJUST', ?, ?, ?, ?, 'ADJUSTMENT', ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, req.getVariantId());
            ps.setObject(2, req.getLotId());
            ps.setObject(3, req.getCopyId());
            ps.setInt(4, Math.abs(req.getQuantity()));
            ps.setString(5, req.getReason());
            ps.setString(6, req.getNote());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void updateLotQtyAvailable(Long lotId, int delta) {
        jdbc.update("UPDATE lots SET qty_available = qty_available + ?, updated_at = SYSUTCDATETIME() WHERE id = ?",
                delta, lotId);
    }

    public void updateLotQtyDamaged(Long lotId, int delta) {
        jdbc.update("UPDATE lots SET qty_damaged = qty_damaged + ?, updated_at = SYSUTCDATETIME() WHERE id = ?", delta,
                lotId);
    }

    private AdjustmentDTO mapDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        AdjustmentDTO d = new AdjustmentDTO();
        d.setId(rs.getLong("id"));
        d.setMovementType(rs.getString("movement_type"));
        d.setVariantId(rs.getObject("variant_id") != null ? rs.getLong("variant_id") : null);
        d.setVariantSku(rs.getString("variant_sku"));
        d.setBookTitle(rs.getString("book_title"));
        d.setLotId(rs.getObject("lot_id") != null ? rs.getLong("lot_id") : null);
        d.setLotCode(rs.getString("lot_code"));
        d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
        d.setCopyCode(rs.getString("copy_code"));
        d.setQuantity(rs.getInt("quantity"));
        d.setFromLocation(rs.getString("from_location"));
        d.setToLocation(rs.getString("to_location"));
        d.setReferenceType(rs.getString("reference_type"));
        d.setReferenceId(rs.getObject("reference_id") != null ? rs.getLong("reference_id") : null);
        d.setReason(rs.getString("reason"));
        d.setNote(rs.getString("note"));
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        d.setCreatedByName(rs.getString("created_by_name"));
        return d;
    }
}

