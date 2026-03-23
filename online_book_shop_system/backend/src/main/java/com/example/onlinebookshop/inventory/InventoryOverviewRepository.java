package com.example.onlinebookshop.inventory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryOverviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryOverviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StockByVariantDTO> getStockByVariant() {
        String sql = "SELECT v.id AS variantId, v.sku, b.title, v.format, ISNULL(SUM(l.qty_available), 0) AS totalAvailable "
                +
                "FROM book_variants v " +
                "JOIN books b ON v.book_id = b.id " +
                "LEFT JOIN lots l ON l.variant_id = v.id AND l.deleted_at IS NULL " +
                "WHERE v.deleted_at IS NULL AND b.deleted_at IS NULL " +
                "GROUP BY v.id, v.sku, b.title, v.format";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StockByVariantDTO dto = new StockByVariantDTO();
            dto.setVariantId(rs.getLong("variantId"));
            dto.setSku(rs.getString("sku"));
            dto.setTitle(rs.getString("title"));
            dto.setFormat(rs.getString("format"));
            dto.setTotalAvailable(rs.getInt("totalAvailable"));
            return dto;
        });
    }

    public List<StockByLotDTO> getStockByLot() {
        String sql = "SELECT l.id AS lotId, l.lot_code, v.id AS variantId, v.sku, b.title, " +
                "l.qty_received, l.qty_available, l.condition_default, l.received_at " +
                "FROM lots l " +
                "JOIN book_variants v ON l.variant_id = v.id " +
                "JOIN books b ON v.book_id = b.id " +
                "WHERE l.deleted_at IS NULL AND v.deleted_at IS NULL";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StockByLotDTO dto = new StockByLotDTO();
            dto.setLotId(rs.getLong("lotId"));
            dto.setLotCode(rs.getString("lot_code"));
            dto.setVariantId(rs.getLong("variantId"));
            dto.setSku(rs.getString("sku"));
            dto.setTitle(rs.getString("title"));
            dto.setQtyReceived(rs.getInt("qty_received"));
            dto.setQtyAvailable(rs.getInt("qty_available"));
            dto.setConditionDefault(rs.getString("condition_default"));
            if (rs.getTimestamp("received_at") != null) {
                dto.setReceivedAt(rs.getTimestamp("received_at").toLocalDateTime());
            }
            return dto;
        });
    }

    public List<StockByConditionDTO> getStockByCondition() {
        String sql = "SELECT c.condition_grade AS condition, COUNT(c.id) AS totalAvailable " +
                "FROM copies c " +
                "WHERE c.status = 'AVAILABLE' AND c.deleted_at IS NULL " +
                "GROUP BY c.condition_grade";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StockByConditionDTO dto = new StockByConditionDTO();
            dto.setCondition(rs.getString("condition"));
            dto.setTotalAvailable(rs.getInt("totalAvailable"));
            return dto;
        });
    }

    public List<LowStockAlertDTO> getLowStockAlerts(int threshold) {
        String sql = "SELECT v.id AS variantId, v.sku, b.title, ISNULL(SUM(l.qty_available), 0) AS totalAvailable " +
                "FROM book_variants v " +
                "JOIN books b ON v.book_id = b.id " +
                "LEFT JOIN lots l ON l.variant_id = v.id AND l.deleted_at IS NULL " +
                "WHERE v.deleted_at IS NULL AND b.deleted_at IS NULL " +
                "GROUP BY v.id, v.sku, b.title " +
                "HAVING ISNULL(SUM(l.qty_available), 0) <= ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LowStockAlertDTO dto = new LowStockAlertDTO();
            dto.setVariantId(rs.getLong("variantId"));
            dto.setSku(rs.getString("sku"));
            dto.setTitle(rs.getString("title"));
            dto.setTotalAvailable(rs.getInt("totalAvailable"));
            dto.setThreshold(threshold);
            return dto;
        }, threshold);
    }

    public List<OverstockAlertDTO> getAgingLotAlerts(int ageDaysThreshold) {
        String sql = "SELECT l.id AS lotId, l.lot_code, v.id AS variantId, v.sku, b.title, " +
                "l.qty_available, DATEDIFF(day, l.received_at, GETDATE()) AS ageDays " +
                "FROM lots l " +
                "JOIN book_variants v ON l.variant_id = v.id " +
                "JOIN books b ON v.book_id = b.id " +
                "WHERE l.qty_available > 0 AND l.deleted_at IS NULL " +
                "AND DATEDIFF(day, l.received_at, GETDATE()) > ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OverstockAlertDTO dto = new OverstockAlertDTO();
            dto.setLotId(rs.getLong("lotId"));
            dto.setLotCode(rs.getString("lot_code"));
            dto.setVariantId(rs.getLong("variantId"));
            dto.setSku(rs.getString("sku"));
            dto.setTitle(rs.getString("title"));
            dto.setQtyAvailable(rs.getInt("qty_available"));
            dto.setAgeDays(rs.getLong("ageDays"));
            return dto;
        }, ageDaysThreshold);
    }
}

