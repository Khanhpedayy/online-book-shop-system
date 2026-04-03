package com.example.onlinebookshop.shipping;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShippingSettingsRepository {

    private static final String GROUP_SHIPPING = "SHIPPING";
    private static final String KEY_RULES = "SHIPPING_RULES";

    private final JdbcTemplate jdbc;

    public ShippingSettingsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * JSON in {@code settings.value_json}, e.g. {@code {"freeAbove":500000,"standardFee":30000,...}}.
     * Only {@code freeAbove} is used by the storefront shipping rules.
     */
    public Optional<String> findShippingRulesJson() {
        try {
            String sql = "SELECT TOP 1 value_json FROM settings WHERE [group] = ? AND [key] = ?";
            List<String> rows = jdbc.query(sql, (rs, i) -> rs.getString(1), GROUP_SHIPPING, KEY_RULES);
            return rows.stream().findFirst().filter(s -> s != null && !s.isBlank());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
