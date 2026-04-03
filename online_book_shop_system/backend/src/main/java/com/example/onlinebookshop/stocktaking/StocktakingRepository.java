package com.example.onlinebookshop.stocktaking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StocktakingRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StocktakingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ── Get expected stock by lot ── */
    public List<StocktakingEntryDTO> getExpectedStock(String scope) {
        boolean hasScope = scope != null && !scope.isBlank();

        // Khi kiểm kê toàn bộ: chỉ lấy lô còn hàng (qty > 0).
        // Khi kiểm kê theo lô/SKU cụ thể: lấy cả lô qty = 0 vì mục đích là xác minh thực tế.
        String qtyFilter = hasScope ? "l.qty_available >= 0 " : "l.qty_available > 0 ";

        StringBuilder sql = new StringBuilder(
                "SELECT l.variant_id, v.sku, b.title, l.id AS lot_id, l.lot_code, l.qty_available AS expected_qty "
                        + "FROM lots l "
                        + "JOIN book_variants v ON l.variant_id = v.id "
                        + "JOIN books b ON v.book_id = b.id "
                        + "WHERE l.deleted_at IS NULL AND " + qtyFilter);

        if (hasScope) {
            if (scope.startsWith("VARIANT:")) {
                String sku = scope.substring(8).trim();
                if (sku.isEmpty())
                    throw new IllegalArgumentException("Vui lòng nhập mã SKU sau 'VARIANT:'");
                sql.append("AND v.sku = N'").append(sku.replace("'", "''")).append("' ");
            } else if (scope.startsWith("LOT:")) {
                String lotCode = scope.substring(4).trim();
                if (lotCode.isEmpty())
                    throw new IllegalArgumentException("Vui lòng nhập mã lô sau 'LOT:'");
                sql.append("AND l.lot_code = '").append(lotCode.replace("'", "''")).append("' ");
            } else {
                throw new IllegalArgumentException(
                    "Scope không hợp lệ: '" + scope + "'. Dùng 'LOT:{lot_code}' hoặc 'VARIANT:{sku}', hoặc bỏ trống.");
            }
        }
        // scope null hoặc trống → kiểm kê toàn bộ
        sql.append("ORDER BY v.sku, l.lot_code");

        return jdbc.query(sql.toString(), (rs, i) -> {
            StocktakingEntryDTO d = new StocktakingEntryDTO();
            d.setVariantId(rs.getLong("variant_id"));
            d.setSku(rs.getString("sku"));
            d.setTitle(rs.getString("title"));
            d.setLotId(rs.getLong("lot_id"));
            d.setLotCode(rs.getString("lot_code"));
            d.setExpectedQty(rs.getInt("expected_qty"));
            return d;
        });
    }

    /* ── Save session to settings ── */
    public void saveSession(StocktakingSessionDTO session) {
        try {
            String json = objectMapper.writeValueAsString(session);
            int rows = jdbc.update(
                    "UPDATE settings SET value_json = ?, updated_at = SYSUTCDATETIME() "
                            + "WHERE [group] = 'STOCKTAKING' AND [key] = ?",
                    json, session.getSessionCode());
            if (rows == 0) {
                jdbc.update(
                        "INSERT INTO settings ([group], [key], value_json, description) VALUES ('STOCKTAKING', ?, ?, N'Stocktaking session')",
                        session.getSessionCode(), json);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save stocktaking session", e);
        }
    }

    /* ── Get session from settings ── */
    public StocktakingSessionDTO getSession(String sessionCode) {
        String sql = "SELECT value_json FROM settings WHERE [group] = 'STOCKTAKING' AND [key] = ?";
        List<String> list = jdbc.query(sql, (rs, i) -> rs.getString("value_json"), sessionCode);
        if (list.isEmpty())
            return null;
        try {
            return objectMapper.readValue(list.get(0), StocktakingSessionDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /* ── List all sessions ── */
    public List<StocktakingSessionDTO> getAllSessions() {
        String sql = "SELECT value_json FROM settings WHERE [group] = 'STOCKTAKING' ORDER BY updated_at DESC";
        List<String> jsons = jdbc.query(sql, (rs, i) -> rs.getString("value_json"));
        List<StocktakingSessionDTO> sessions = new ArrayList<>();
        for (String json : jsons) {
            try {
                sessions.add(objectMapper.readValue(json, StocktakingSessionDTO.class));
            } catch (Exception ignored) {
            }
        }
        return sessions;
    }

    /* ── Update lot qty ── */
    public void updateLotQtyAvailable(Long lotId, int delta) {
        jdbc.update("UPDATE lots SET qty_available = qty_available + ?, updated_at = SYSUTCDATETIME() WHERE id = ?",
                delta, lotId);
    }

    /* ── Log adjustment transaction ── */
    // delta > 0: thực đếm nhiều hơn sổ sách → nhập thêm (IN)
    // delta < 0: thực đếm ít hơn sổ sách  → xuất bớt (OUT)
    // Cột quantity phải > 0 (CK_it_qty) → dùng Math.abs, hướng encode qua movement_type
    public void logAdjustment(Long variantId, Long lotId, int delta, String note) {
        String movementType = delta > 0 ? "IN" : "OUT";
        jdbc.update("INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, quantity, "
                        + "reference_type, reason, note) VALUES (?, ?, ?, ?, 'STOCKTAKING', 'COUNT_DIFF', ?)",
                movementType, variantId, lotId, Math.abs(delta), note);
    }
}
