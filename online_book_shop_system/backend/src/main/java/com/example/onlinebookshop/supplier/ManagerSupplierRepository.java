package com.example.onlinebookshop.supplier;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ManagerSupplierRepository {

    private final JdbcTemplate jdbc;

    public ManagerSupplierRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SupplierDTO> findAll() {
        String sql = "SELECT s.id, s.name, s.code, s.email, s.phone, s.address, s.contact_person, s.is_active, "
                + "s.tax_id, s.payment_terms, "
                + "s.created_at, s.updated_at, "
                + "ISNULL(agg.totalLots, 0) AS totalLots, ISNULL(agg.totalQtyReceived, 0) AS totalQtyReceived "
                + "FROM suppliers s "
                + "LEFT JOIN ("
                + "  SELECT supplier_id, COUNT(*) AS totalLots, SUM(qty_received) AS totalQtyReceived "
                + "  FROM lots WHERE deleted_at IS NULL GROUP BY supplier_id"
                + ") agg ON agg.supplier_id = s.id "
                + "WHERE s.deleted_at IS NULL "
                + "ORDER BY s.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            SupplierDTO d = new SupplierDTO();
            d.setId(rs.getLong("id"));
            d.setName(rs.getString("name"));
            d.setCode(rs.getString("code"));
            d.setEmail(rs.getString("email"));
            d.setPhone(rs.getString("phone"));
            d.setAddress(rs.getString("address"));
            d.setContactPerson(rs.getString("contact_person"));
            d.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            if (rs.getTimestamp("updated_at") != null)
                d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
            d.setTotalLots(rs.getInt("totalLots"));
            d.setTotalQtyReceived(rs.getInt("totalQtyReceived"));
            d.setTaxId(rs.getString("tax_id"));
            d.setPaymentTerms(rs.getString("payment_terms"));
            return d;
        });
    }

    public SupplierDTO findById(Long id) {
        String sql = "SELECT s.id, s.name, s.code, s.email, s.phone, s.address, s.contact_person, s.is_active, "
                + "s.tax_id, s.payment_terms, "
                + "s.created_at, s.updated_at, "
                + "ISNULL(agg.totalLots, 0) AS totalLots, ISNULL(agg.totalQtyReceived, 0) AS totalQtyReceived "
                + "FROM suppliers s "
                + "LEFT JOIN ("
                + "  SELECT supplier_id, COUNT(*) AS totalLots, SUM(qty_received) AS totalQtyReceived "
                + "  FROM lots WHERE deleted_at IS NULL GROUP BY supplier_id"
                + ") agg ON agg.supplier_id = s.id "
                + "WHERE s.id = ? AND s.deleted_at IS NULL";
        List<SupplierDTO> list = jdbc.query(sql, (rs, i) -> {
            SupplierDTO d = new SupplierDTO();
            d.setId(rs.getLong("id"));
            d.setName(rs.getString("name"));
            d.setCode(rs.getString("code"));
            d.setEmail(rs.getString("email"));
            d.setPhone(rs.getString("phone"));
            d.setAddress(rs.getString("address"));
            d.setContactPerson(rs.getString("contact_person"));
            d.setIsActive(rs.getBoolean("is_active"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            if (rs.getTimestamp("updated_at") != null)
                d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
            d.setTotalLots(rs.getInt("totalLots"));
            d.setTotalQtyReceived(rs.getInt("totalQtyReceived"));
            d.setTaxId(rs.getString("tax_id"));
            d.setPaymentTerms(rs.getString("payment_terms"));
            return d;
        }, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(CreateSupplierRequest req) {
        String sql = "INSERT INTO suppliers (name, code, email, phone, address, contact_person, tax_id, payment_terms) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getName());
            ps.setString(2, req.getCode());
            ps.setString(3, req.getEmail());
            ps.setString(4, req.getPhone());
            ps.setString(5, req.getAddress());
            ps.setString(6, req.getContactPerson());
            ps.setString(7, req.getTaxId());
            ps.setString(8, req.getPaymentTerms());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public int update(Long id, UpdateSupplierRequest req) {
        String sql = "UPDATE suppliers SET name=?, code=?, email=?, phone=?, address=?, contact_person=?, tax_id=?, payment_terms=?, "
                + "is_active=?, updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL";
        return jdbc.update(sql, req.getName(), req.getCode(), req.getEmail(), req.getPhone(),
                req.getAddress(), req.getContactPerson(), req.getTaxId(), req.getPaymentTerms(), req.getIsActive(), id);
    }

    public int softDelete(Long id) {
        return jdbc.update("UPDATE suppliers SET deleted_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL", id);
    }
}

