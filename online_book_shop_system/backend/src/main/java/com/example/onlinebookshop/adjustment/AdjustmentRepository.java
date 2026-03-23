package com.example.onlinebookshop.adjustment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class AdjustmentRepository {

    private final JdbcTemplate jdbc;

    public AdjustmentRepository(JdbcTemplate jdbc) {
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
                + "WHERE it.reference_type = 'ADJUSTMENT' "
                + "ORDER BY it.created_at DESC";
        return jdbc.query(sql, (rs, i) -> mapDTO(rs));
    }

    public Long insert(CreateAdjustmentRequest req) {
        String sql = "INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, copy_id, "
                + "quantity, reference_type, reason, note, created_by) VALUES ('ADJUST', ?, ?, ?, ?, 'ADJUSTMENT', ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, req.getVariantId());
            ps.setObject(2, req.getLotId());
            ps.setObject(3, req.getCopyId());
            ps.setInt(4, Math.abs(req.getQuantity())); // Must be strictly positive for CK_it_qty

            // Pack both TYPE and DIRECTION into DB REASON to preserve them
            String typeStr = req.getType() != null ? req.getType() : "DAMAGE";
            String dirStr = req.getDirection() != null ? req.getDirection() : "OUT";
            ps.setString(5, typeStr + "_" + dirStr);

            String customNote = req.getReason() != null ? req.getReason() : "";
            if (req.getNote() != null && !req.getNote().isBlank()) {
                customNote = (!customNote.isBlank() ? customNote + " | " : "") + req.getNote();
            }
            ps.setString(6, customNote); // Store Frontend REASON in DB NOTE
            ps.setObject(7, req.getCreatedBy());
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

        // Unpack DB REASON back to Frontend TYPE and DIRECTION
        String dbReason = rs.getString("reason");
        if (dbReason != null && dbReason.contains("_")) {
            int lastIdx = dbReason.lastIndexOf("_");
            d.setType(dbReason.substring(0, lastIdx));
            d.setDirection(dbReason.substring(lastIdx + 1));
        } else {
            d.setType(dbReason);
            d.setDirection("OUT"); // Fallback
        }

        d.setVariantId(rs.getObject("variant_id") != null ? rs.getLong("variant_id") : null);
        d.setVariantSku(rs.getString("variant_sku"));
        d.setBookTitle(rs.getString("book_title"));
        d.setLotId(rs.getObject("lot_id") != null ? rs.getLong("lot_id") : null);
        d.setLotCode(rs.getString("lot_code"));
        d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
        d.setCopyCode(rs.getString("copy_code"));
        d.setQuantity(Math.abs(rs.getInt("quantity"))); // Already positive
        d.setFromLocation(rs.getString("from_location"));
        d.setToLocation(rs.getString("to_location"));
        d.setReferenceType(rs.getString("reference_type"));
        d.setReferenceId(rs.getObject("reference_id") != null ? rs.getLong("reference_id") : null);

        // Map DB NOTE (which stores Frontend REASON) back to Frontend REASON
        d.setReason(rs.getString("note"));
        d.setNote(rs.getString("note"));

        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        d.setCreatedByName(rs.getString("created_by_name"));
        return d;
    }
}

