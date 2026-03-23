package com.example.onlinebookshop.allocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AllocationRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AllocationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AllocationSettingsDTO getSettings() {
        String sql = "SELECT value_json FROM settings WHERE [group] = 'INVENTORY' AND [key] = 'ALLOCATION'";
        List<String> list = jdbc.query(sql, (rs, i) -> rs.getString("value_json"));
        if (list.isEmpty()) {
            return new AllocationSettingsDTO("LOT", 30, "NEWEST_FIRST", false);
        }
        try {
            return objectMapper.readValue(list.get(0), AllocationSettingsDTO.class);
        } catch (Exception e) {
            return new AllocationSettingsDTO("LOT", 30, "NEWEST_FIRST", false);
        }
    }

    public void saveSettings(AllocationSettingsDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            int rows = jdbc.update(
                    "UPDATE settings SET value_json = ?, updated_at = SYSUTCDATETIME() WHERE [group] = 'INVENTORY' AND [key] = 'ALLOCATION'",
                    json);
            if (rows == 0) {
                jdbc.update(
                        "INSERT INTO settings ([group], [key], value_json, description) VALUES ('INVENTORY', 'ALLOCATION', ?, N'Allocation rules')",
                        json);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save allocation settings", e);
        }
    }
}

