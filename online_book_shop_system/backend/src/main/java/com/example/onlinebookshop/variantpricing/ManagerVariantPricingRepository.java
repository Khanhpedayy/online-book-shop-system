package com.example.onlinebookshop.variantpricing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class ManagerVariantPricingRepository {

    private final JdbcTemplate jdbc;

    public ManagerVariantPricingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” LIST BY BOOK â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<VariantDTO> findVariantsByBookId(Long bookId) {
        String sql = """
                    SELECT bv.*, b.title AS book_title,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.deleted_at IS NULL) AS total_copies,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.status = 'AVAILABLE' AND c.deleted_at IS NULL) AS available_copies
                    FROM book_variants bv
                    JOIN books b ON b.id = bv.book_id
                    WHERE bv.book_id = ? AND bv.deleted_at IS NULL
                    ORDER BY bv.id
                """;
        return jdbc.query(sql, (rs, rn) -> mapVariant(rs), bookId);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” LIST ALL â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<VariantDTO> findAllVariants() {
        String sql = """
                    SELECT bv.*, b.title AS book_title,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.deleted_at IS NULL) AS total_copies,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.status = 'AVAILABLE' AND c.deleted_at IS NULL) AS available_copies
                    FROM book_variants bv
                    JOIN books b ON b.id = bv.book_id
                    WHERE bv.deleted_at IS NULL AND b.deleted_at IS NULL
                    ORDER BY b.title, bv.sku
                """;
        return jdbc.query(sql, (rs, rn) -> mapVariant(rs));
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” GET BY ID â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public VariantDTO findVariantById(Long id) {
        String sql = """
                    SELECT bv.*, b.title AS book_title,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.deleted_at IS NULL) AS total_copies,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.status = 'AVAILABLE' AND c.deleted_at IS NULL) AS available_copies
                    FROM book_variants bv
                    JOIN books b ON b.id = bv.book_id
                    WHERE bv.id = ? AND bv.deleted_at IS NULL
                """;
        List<VariantDTO> list = jdbc.query(sql, (rs, rn) -> mapVariant(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” GET BY SKU â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public VariantDTO findVariantBySku(String sku) {
        String sql = """
                    SELECT bv.*, b.title AS book_title,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.deleted_at IS NULL) AS total_copies,
                           (SELECT COUNT(*) FROM copies c WHERE c.variant_id = bv.id AND c.status = 'AVAILABLE' AND c.deleted_at IS NULL) AS available_copies
                    FROM book_variants bv
                    JOIN books b ON b.id = bv.book_id
                    WHERE bv.sku = ? AND bv.deleted_at IS NULL
                """;
        List<VariantDTO> list = jdbc.query(sql, (rs, rn) -> mapVariant(rs), sku);
        return list.isEmpty() ? null : list.get(0);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” CREATE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public Long insertVariant(CreateVariantRequest req) {
        String sql = """
                    INSERT INTO book_variants (book_id, sku, format, edition, language,
                                               list_price, sale_price,
                                               page_count, weight_grams, width_mm, height_mm, thickness_mm,
                                               is_active, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, SYSUTCDATETIME())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.getBookId());
            ps.setString(2, req.getSku());
            ps.setString(3, req.getFormat());
            ps.setString(4, req.getEdition());
            ps.setString(5, req.getLanguage());
            ps.setDouble(6, req.getListPrice() != null ? req.getListPrice() : 0);
            ps.setDouble(7, req.getSalePrice() != null ? req.getSalePrice() : 0);
            ps.setObject(8, req.getPageCount());
            ps.setObject(9, req.getWeightGrams());
            ps.setObject(10, req.getWidthMm());
            ps.setObject(11, req.getHeightMm());
            ps.setObject(12, req.getThicknessMm());
            return ps;
        }, kh);
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” UPDATE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int updateVariant(Long id, UpdateVariantRequest req) {
        String sql = """
                    UPDATE book_variants SET
                        sku = ?, format = ?, edition = ?, language = ?,
                        list_price = ?, sale_price = ?,
                        page_count = ?, weight_grams = ?, width_mm = ?, height_mm = ?, thickness_mm = ?,
                        is_active = ?,
                        updated_at = SYSUTCDATETIME()
                    WHERE id = ? AND deleted_at IS NULL
                """;
        return jdbc.update(sql,
                req.getSku(), req.getFormat(), req.getEdition(), req.getLanguage(),
                req.getListPrice(), req.getSalePrice(),
                req.getPageCount(), req.getWeightGrams(), req.getWidthMm(), req.getHeightMm(), req.getThicknessMm(),
                req.getIsActive() != null && req.getIsActive() ? 1 : 0,
                id);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” SET BASE PRICE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int setBasePrice(Long variantId, Double listPrice, Double salePrice) {
        String sql = "UPDATE book_variants SET list_price = ?, sale_price = ?, updated_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, listPrice, salePrice, variantId);
    }

    /*
     * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” SET CONDITION PRICES â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
     */

    public int setConditionPrices(Long variantId, String conditionPricesJson) {
        String sql = "UPDATE book_variants SET condition_prices_json = ?, updated_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, conditionPricesJson, variantId);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT â€” SOFT DELETE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int softDeleteVariant(Long id) {
        String sql = "UPDATE book_variants SET deleted_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, id);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• COPIES â€” LIST BY VARIANT â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<CopyPricingDTO> findCopiesByVariantId(Long variantId) {
        String sql = """
                    SELECT c.id, c.copy_code, c.variant_id, bv.sku AS variant_sku,
                           c.lot_id, l.lot_code,
                           c.condition_grade, c.condition_note,
                           c.has_signature, c.is_first_edition,
                           c.sell_price_override, c.status, c.location, c.created_at
                    FROM copies c
                    JOIN book_variants bv ON bv.id = c.variant_id
                    LEFT JOIN lots l ON l.id = c.lot_id
                    WHERE c.variant_id = ? AND c.deleted_at IS NULL
                    ORDER BY c.copy_code
                """;
        return jdbc.query(sql, (rs, rn) -> {
            CopyPricingDTO dto = new CopyPricingDTO();
            dto.setId(rs.getLong("id"));
            dto.setCopyCode(rs.getString("copy_code"));
            dto.setVariantId(rs.getLong("variant_id"));
            dto.setVariantSku(rs.getString("variant_sku"));
            dto.setLotId(rs.getObject("lot_id", Long.class));
            dto.setLotCode(rs.getString("lot_code"));
            dto.setConditionGrade(rs.getString("condition_grade"));
            dto.setConditionNote(rs.getString("condition_note"));
            dto.setHasSignature(rs.getBoolean("has_signature"));
            dto.setIsFirstEdition(rs.getBoolean("is_first_edition"));
            dto.setSellPriceOverride(rs.getObject("sell_price_override", Double.class));
            dto.setStatus(rs.getString("status"));
            dto.setLocation(rs.getString("location"));
            dto.setCreatedAt(rs.getString("created_at"));
            return dto;
        }, variantId);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• COPY â€” OVERRIDE PRICE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int overrideCopyPrice(Long copyId, Double sellPriceOverride) {
        String sql = "UPDATE copies SET sell_price_override = ?, updated_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, sellPriceOverride, copyId);
    }

    /* â•â•â• Helper â•â•â• */

    private VariantDTO mapVariant(java.sql.ResultSet rs) throws java.sql.SQLException {
        VariantDTO dto = new VariantDTO();
        dto.setId(rs.getLong("id"));
        dto.setBookId(rs.getLong("book_id"));
        dto.setBookTitle(rs.getString("book_title"));
        dto.setSku(rs.getString("sku"));
        dto.setFormat(rs.getString("format"));
        dto.setEdition(rs.getString("edition"));
        dto.setLanguage(rs.getString("language"));
        dto.setListPrice(rs.getDouble("list_price"));
        dto.setSalePrice(rs.getDouble("sale_price"));
        dto.setConditionPricesJson(rs.getString("condition_prices_json"));
        dto.setPageCount(rs.getObject("page_count", Integer.class));
        dto.setWeightGrams(rs.getObject("weight_grams", Integer.class));
        dto.setWidthMm(rs.getObject("width_mm", Integer.class));
        dto.setHeightMm(rs.getObject("height_mm", Integer.class));
        dto.setThicknessMm(rs.getObject("thickness_mm", Integer.class));
        dto.setIsActive(rs.getBoolean("is_active"));
        dto.setCreatedAt(rs.getString("created_at"));
        dto.setUpdatedAt(rs.getString("updated_at"));
        dto.setTotalCopies(rs.getInt("total_copies"));
        dto.setAvailableCopies(rs.getInt("available_copies"));
        return dto;
    }
}

