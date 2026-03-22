package com.example.onlinebookshop.copy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ManagerCopyRepository {

    private final JdbcTemplate jdbc;

    public ManagerCopyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â”€â”€ Search â”€â”€ */
    public List<CopyDTO> search(String query) {
        String sql = "SELECT c.id, c.copy_code, c.lot_id, l.lot_code, c.variant_id, v.sku AS variant_sku, "
                + "b.title AS book_title, c.location, c.condition_grade, c.condition_note, "
                + "c.has_signature, c.is_first_edition, c.attributes_json, c.images_json, "
                + "c.sell_price_override, c.status, c.reserved_at, c.reserve_expires_at, "
                + "c.created_at, c.updated_at "
                + "FROM copies c "
                + "JOIN lots l ON c.lot_id = l.id "
                + "JOIN book_variants v ON c.variant_id = v.id "
                + "JOIN books b ON v.book_id = b.id "
                + "WHERE c.deleted_at IS NULL AND (c.copy_code LIKE ? OR l.lot_code LIKE ? OR v.sku LIKE ? OR b.title LIKE ?) "
                + "ORDER BY c.created_at DESC";
        String pat = "%" + query + "%";
        return jdbc.query(sql, (rs, i) -> mapCopyDTO(rs), pat, pat, pat, pat);
    }

    /* â”€â”€ Get all copies (paginated) â”€â”€ */
    public List<CopyDTO> findAll(Long variantId, Long lotId, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.copy_code, c.lot_id, l.lot_code, c.variant_id, v.sku AS variant_sku, "
                        + "b.title AS book_title, c.location, c.condition_grade, c.condition_note, "
                        + "c.has_signature, c.is_first_edition, c.attributes_json, c.images_json, "
                        + "c.sell_price_override, c.status, c.reserved_at, c.reserve_expires_at, "
                        + "c.created_at, c.updated_at "
                        + "FROM copies c "
                        + "JOIN lots l ON c.lot_id = l.id "
                        + "JOIN book_variants v ON c.variant_id = v.id "
                        + "JOIN books b ON v.book_id = b.id "
                        + "WHERE c.deleted_at IS NULL ");
        java.util.List<Object> params = new java.util.ArrayList<>();
        if (variantId != null) {
            sql.append("AND c.variant_id = ? ");
            params.add(variantId);
        }
        if (lotId != null) {
            sql.append("AND c.lot_id = ? ");
            params.add(lotId);
        }
        if (status != null) {
            sql.append("AND c.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY c.created_at DESC");
        return jdbc.query(sql.toString(), (rs, i) -> mapCopyDTO(rs), params.toArray());
    }

    /* â”€â”€ Single copy â”€â”€ */
    public CopyDTO findById(Long id) {
        String sql = "SELECT c.id, c.copy_code, c.lot_id, l.lot_code, c.variant_id, v.sku AS variant_sku, "
                + "b.title AS book_title, c.location, c.condition_grade, c.condition_note, "
                + "c.has_signature, c.is_first_edition, c.attributes_json, c.images_json, "
                + "c.sell_price_override, c.status, c.reserved_at, c.reserve_expires_at, "
                + "c.created_at, c.updated_at "
                + "FROM copies c "
                + "JOIN lots l ON c.lot_id = l.id "
                + "JOIN book_variants v ON c.variant_id = v.id "
                + "JOIN books b ON v.book_id = b.id "
                + "WHERE c.id = ? AND c.deleted_at IS NULL";
        List<CopyDTO> list = jdbc.query(sql, (rs, i) -> mapCopyDTO(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /* â”€â”€ Lifecycle: supplier + lot + transactions â”€â”€ */
    public CopyLifecycleDTO findLifecycleById(Long id) {
        CopyDTO base = findById(id);
        if (base == null)
            return null;
        CopyLifecycleDTO lc = new CopyLifecycleDTO();
        lc.setId(base.getId());
        lc.setCopyCode(base.getCopyCode());
        lc.setLotId(base.getLotId());
        lc.setLotCode(base.getLotCode());
        lc.setVariantId(base.getVariantId());
        lc.setVariantSku(base.getVariantSku());
        lc.setBookTitle(base.getBookTitle());
        lc.setLocation(base.getLocation());
        lc.setConditionGrade(base.getConditionGrade());
        lc.setConditionNote(base.getConditionNote());
        lc.setHasSignature(base.getHasSignature());
        lc.setIsFirstEdition(base.getIsFirstEdition());
        lc.setAttributesJson(base.getAttributesJson());
        lc.setImagesJson(base.getImagesJson());
        lc.setSellPriceOverride(base.getSellPriceOverride());
        lc.setStatus(base.getStatus());
        lc.setReservedAt(base.getReservedAt());
        lc.setReserveExpiresAt(base.getReserveExpiresAt());
        lc.setCreatedAt(base.getCreatedAt());
        lc.setUpdatedAt(base.getUpdatedAt());

        // Supplier + lot info
        String lotSql = "SELECT s.name, l.received_at, l.unit_cost FROM lots l JOIN suppliers s ON l.supplier_id = s.id WHERE l.id = ?";
        jdbc.query(lotSql, (rs, i) -> {
            lc.setSupplierName(rs.getString("name"));
            if (rs.getTimestamp("received_at") != null)
                lc.setReceivedAt(rs.getTimestamp("received_at").toLocalDateTime().toString());
            lc.setUnitCost(rs.getDouble("unit_cost"));
            return null;
        }, base.getLotId());

        // Transactions
        lc.setTransactions(findTransactionsByCopy(id));
        return lc;
    }

    public List<CopyTransactionDTO> findTransactionsByCopy(Long copyId) {
        String sql = "SELECT id, movement_type, quantity, from_location, to_location, "
                + "reference_type, reference_id, reason, note, created_at "
                + "FROM inventory_transactions WHERE copy_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            CopyTransactionDTO t = new CopyTransactionDTO();
            t.setId(rs.getLong("id"));
            t.setMovementType(rs.getString("movement_type"));
            t.setQuantity(rs.getInt("quantity"));
            t.setFromLocation(rs.getString("from_location"));
            t.setToLocation(rs.getString("to_location"));
            t.setReferenceType(rs.getString("reference_type"));
            t.setReferenceId(rs.getObject("reference_id") != null ? rs.getLong("reference_id") : null);
            t.setReason(rs.getString("reason"));
            t.setNote(rs.getString("note"));
            if (rs.getTimestamp("created_at") != null)
                t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return t;
        }, copyId);
    }

    /* â”€â”€ Update: condition â”€â”€ */
    public int updateCondition(Long id, String conditionGrade, String conditionNote) {
        return jdbc.update(
                "UPDATE copies SET condition_grade=?, condition_note=?, updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL",
                conditionGrade, conditionNote, id);
    }

    /* â”€â”€ Update: location â”€â”€ */
    public int updateLocation(Long id, String newLocation) {
        return jdbc.update(
                "UPDATE copies SET location=?, updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL",
                newLocation, id);
    }

    /* â”€â”€ Update: status â”€â”€ */
    public int updateStatus(Long id, String status) {
        return jdbc.update("UPDATE copies SET status=?, updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL",
                status, id);
    }

    /* â”€â”€ Update: photos â”€â”€ */
    public int updatePhotos(Long id, String imagesJson) {
        return jdbc.update(
                "UPDATE copies SET images_json=?, updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL",
                imagesJson, id);
    }

    /* â”€â”€ Log transaction â”€â”€ */
    public void logTransaction(String movementType, Long variantId, Long lotId, Long copyId,
                               int qty, String fromLoc, String toLoc, String refType, Long refId, String reason, String note) {
        String sql = "INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, copy_id, quantity, "
                + "from_location, to_location, reference_type, reference_id, reason, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, movementType, variantId, lotId, copyId, qty, fromLoc, toLoc, refType, refId, reason, note);
    }

    /* â”€â”€ Row mapper â”€â”€ */
    private CopyDTO mapCopyDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        CopyDTO d = new CopyDTO();
        d.setId(rs.getLong("id"));
        d.setCopyCode(rs.getString("copy_code"));
        d.setLotId(rs.getLong("lot_id"));
        d.setLotCode(rs.getString("lot_code"));
        d.setVariantId(rs.getLong("variant_id"));
        d.setVariantSku(rs.getString("variant_sku"));
        d.setBookTitle(rs.getString("book_title"));
        d.setLocation(rs.getString("location"));
        d.setConditionGrade(rs.getString("condition_grade"));
        d.setConditionNote(rs.getString("condition_note"));
        d.setHasSignature(rs.getBoolean("has_signature"));
        d.setIsFirstEdition(rs.getBoolean("is_first_edition"));
        d.setAttributesJson(rs.getString("attributes_json"));
        d.setImagesJson(rs.getString("images_json"));
        d.setSellPriceOverride(
                rs.getObject("sell_price_override") != null ? rs.getDouble("sell_price_override") : null);
        d.setStatus(rs.getString("status"));
        if (rs.getTimestamp("reserved_at") != null)
            d.setReservedAt(rs.getTimestamp("reserved_at").toLocalDateTime().toString());
        if (rs.getTimestamp("reserve_expires_at") != null)
            d.setReserveExpiresAt(rs.getTimestamp("reserve_expires_at").toLocalDateTime().toString());
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        if (rs.getTimestamp("updated_at") != null)
            d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
        return d;
    }
}

