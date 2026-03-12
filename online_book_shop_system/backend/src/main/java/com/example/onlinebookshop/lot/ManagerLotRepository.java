package com.example.onlinebookshop.lot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ManagerLotRepository {

    private final JdbcTemplate jdbc;

    public ManagerLotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â”€â”€ Lot list â”€â”€ */
    public List<LotDTO> findAll(Long supplierId, Long variantId) {
        StringBuilder sql = new StringBuilder(
                "SELECT l.id, l.lot_code, l.supplier_id, s.name AS supplier_name, "
                        + "l.variant_id, v.sku AS variant_sku, b.title AS book_title, "
                        + "l.receipt_code, l.invoice_no, l.warehouse, l.received_at, "
                        + "l.unit_cost, l.qty_received, l.qty_available, l.qty_reserved, "
                        + "l.qty_sold, l.qty_damaged, l.qty_returned, "
                        + "l.condition_default, l.status, l.note, l.created_at, "
                        + "DATEDIFF(day, l.received_at, GETDATE()) AS age_days, "
                        + "(l.qty_available * l.unit_cost) AS total_cost_value "
                        + "FROM lots l "
                        + "JOIN suppliers s ON l.supplier_id = s.id "
                        + "JOIN book_variants v ON l.variant_id = v.id "
                        + "JOIN books b ON v.book_id = b.id "
                        + "WHERE l.deleted_at IS NULL ");
        if (supplierId != null)
            sql.append("AND l.supplier_id = ").append(supplierId).append(" ");
        if (variantId != null)
            sql.append("AND l.variant_id = ").append(variantId).append(" ");
        sql.append("ORDER BY l.received_at DESC");

        return jdbc.query(sql.toString(), (rs, i) -> mapLotDTO(rs));
    }

    /* â”€â”€ Single lot â”€â”€ */
    public LotDTO findById(Long id) {
        String sql = "SELECT l.id, l.lot_code, l.supplier_id, s.name AS supplier_name, "
                + "l.variant_id, v.sku AS variant_sku, b.title AS book_title, "
                + "l.receipt_code, l.invoice_no, l.warehouse, l.received_at, "
                + "l.unit_cost, l.qty_received, l.qty_available, l.qty_reserved, "
                + "l.qty_sold, l.qty_damaged, l.qty_returned, "
                + "l.condition_default, l.status, l.note, l.created_at, "
                + "DATEDIFF(day, l.received_at, GETDATE()) AS age_days, "
                + "(l.qty_available * l.unit_cost) AS total_cost_value "
                + "FROM lots l "
                + "JOIN suppliers s ON l.supplier_id = s.id "
                + "JOIN book_variants v ON l.variant_id = v.id "
                + "JOIN books b ON v.book_id = b.id "
                + "WHERE l.id = ? AND l.deleted_at IS NULL";
        List<LotDTO> list = jdbc.query(sql, (rs, i) -> mapLotDTO(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /* â”€â”€ Copies for a lot â”€â”€ */
    public List<LotCopyDTO> findCopiesByLot(Long lotId) {
        String sql = "SELECT id, copy_code, location, condition_grade, condition_note, "
                + "status, sell_price_override, created_at "
                + "FROM copies WHERE lot_id = ? AND deleted_at IS NULL ORDER BY id";
        return jdbc.query(sql, (rs, i) -> {
            LotCopyDTO d = new LotCopyDTO();
            d.setId(rs.getLong("id"));
            d.setCopyCode(rs.getString("copy_code"));
            d.setLocation(rs.getString("location"));
            d.setConditionGrade(rs.getString("condition_grade"));
            d.setConditionNote(rs.getString("condition_note"));
            d.setStatus(rs.getString("status"));
            d.setSellPriceOverride(
                    rs.getObject("sell_price_override") != null ? rs.getDouble("sell_price_override") : null);
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, lotId);
    }

    /* â”€â”€ Create lot â”€â”€ */
    public Long insert(CreateLotRequest req) {
        String sql = "INSERT INTO lots (lot_code, supplier_id, variant_id, receipt_code, invoice_no, "
                + "warehouse, received_at, unit_cost, qty_received, qty_available, condition_default, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getLotCode());
            ps.setLong(2, req.getSupplierId());
            ps.setLong(3, req.getVariantId());
            ps.setString(4, req.getReceiptCode());
            ps.setString(5, req.getInvoiceNo());
            ps.setString(6, req.getWarehouse() != null ? req.getWarehouse() : "MAIN");
            ps.setTimestamp(7, req.getReceivedAt() != null ? Timestamp.valueOf(LocalDateTime.parse(req.getReceivedAt()))
                    : new Timestamp(System.currentTimeMillis()));
            ps.setDouble(8, req.getUnitCost());
            ps.setInt(9, req.getQtyReceived());
            ps.setInt(10, req.getQtyReceived()); // qty_available = qty_received initially
            ps.setString(11, req.getConditionDefault() != null ? req.getConditionDefault() : "NEW");
            ps.setString(12, req.getNote());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    /* â”€â”€ Generate copies â”€â”€ */
    public int generateCopies(Long lotId, Long variantId, int count, String prefix, String location,
            String conditionGrade) {
        String sql = "INSERT INTO copies (copy_code, lot_id, variant_id, location, condition_grade) VALUES (?, ?, ?, ?, ?)";
        for (int i = 1; i <= count; i++) {
            String code = prefix + String.format("%04d", i);
            jdbc.update(sql, code, lotId, variantId, location != null ? location : "A1-01",
                    conditionGrade != null ? conditionGrade : "NEW");
        }
        return count;
    }

    /* â”€â”€ Lock / Unlock â”€â”€ */
    public int lockLot(Long id) {
        return jdbc.update(
                "UPDATE lots SET status='LOCKED', updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL", id);
    }

    public int unlockLot(Long id) {
        return jdbc.update(
                "UPDATE lots SET status='RELEASED', updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL", id);
    }

    /* â”€â”€ Log inventory transaction â”€â”€ */
    public void logTransaction(String movementType, Long variantId, Long lotId, Long copyId,
            int quantity, String refType, Long refId, String reason, String note) {
        String sql = "INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, copy_id, "
                + "quantity, reference_type, reference_id, reason, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, movementType, variantId, lotId, copyId, quantity, refType, refId, reason, note);
    }

    /* â”€â”€ Row mapper â”€â”€ */
    private LotDTO mapLotDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        LotDTO d = new LotDTO();
        d.setId(rs.getLong("id"));
        d.setLotCode(rs.getString("lot_code"));
        d.setSupplierId(rs.getLong("supplier_id"));
        d.setSupplierName(rs.getString("supplier_name"));
        d.setVariantId(rs.getLong("variant_id"));
        d.setVariantSku(rs.getString("variant_sku"));
        d.setBookTitle(rs.getString("book_title"));
        d.setReceiptCode(rs.getString("receipt_code"));
        d.setInvoiceNo(rs.getString("invoice_no"));
        d.setWarehouse(rs.getString("warehouse"));
        if (rs.getTimestamp("received_at") != null)
            d.setReceivedAt(rs.getTimestamp("received_at").toLocalDateTime().toString());
        d.setUnitCost(rs.getDouble("unit_cost"));
        d.setQtyReceived(rs.getInt("qty_received"));
        d.setQtyAvailable(rs.getInt("qty_available"));
        d.setQtyReserved(rs.getInt("qty_reserved"));
        d.setQtySold(rs.getInt("qty_sold"));
        d.setQtyDamaged(rs.getInt("qty_damaged"));
        d.setQtyReturned(rs.getInt("qty_returned"));
        d.setConditionDefault(rs.getString("condition_default"));
        d.setStatus(rs.getString("status"));
        d.setNote(rs.getString("note"));
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        d.setAgeDays(rs.getLong("age_days"));
        d.setTotalCostValue(rs.getDouble("total_cost_value"));
        return d;
    }
}

