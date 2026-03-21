package com.example.onlinebookshop.stocktaking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ManagerStocktakingRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ManagerStocktakingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â”€â”€ Get expected stock by lot â”€â”€ */
    public List<StocktakingEntryDTO> getExpectedStock(String scope) {
        StringBuilder sql = new StringBuilder(
                "SELECT l.variant_id, v.sku, b.title, l.id AS lot_id, l.lot_code, l.qty_available AS expected_qty "
                        + "FROM lots l "
                        + "JOIN book_variants v ON l.variant_id = v.id "
                        + "JOIN books b ON v.book_id = b.id "
                        + "WHERE l.deleted_at IS NULL AND l.qty_available > 0 ");

        if (scope != null && scope.startsWith("VARIANT:")) {
            sql.append("AND l.variant_id = ").append(scope.substring(8)).append(" ");
        } else if (scope != null && scope.startsWith("LOT:")) {
            sql.append("AND l.id = ").append(scope.substring(4)).append(" ");
        }
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

    /* â”€â”€ Save session to settings â”€â”€ */
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

    /* â”€â”€ Get session from settings â”€â”€ */
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

    /* â”€â”€ List all sessions â”€â”€ */
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

    /* â”€â”€ Update lot qty â”€â”€ */
    public void updateLotQtyAvailable(Long lotId, int delta) {
        jdbc.update("UPDATE lots SET qty_available = qty_available + ?, updated_at = SYSUTCDATETIME() WHERE id = ?",
                delta, lotId);
    }

    /* â”€â”€ Log adjustment transaction â”€â”€ */
    public void logAdjustment(Long variantId, Long lotId, int qty, String note) {
        jdbc.update("INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, quantity, "
                        + "reference_type, reason, note) VALUES ('ADJUST', ?, ?, ?, 'STOCKTAKING', 'COUNT_DIFF', ?)",
                variantId, lotId, qty, note);
    }
}

