package com.example.onlinebookshop.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/stock-outs")
public class ManagerStockOutController {

    private final JdbcTemplate jdbc;

    public ManagerStockOutController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * GET: danh sách phiếu lỗi
     */
    @GetMapping
    public List<Map<String, Object>> getStockOuts() {
        return jdbc.queryForList("""
            SELECT 
                id,
                stock_out_code,
                exception_note
            FROM stock_outs
            WHERE has_exception = 1
              AND deleted_at IS NULL
            ORDER BY created_at DESC
        """);
    }

    /**
     * GET: chi tiết phiếu
     */
    @GetMapping("/{id}")
    public Map<String, Object> getStockOutDetail(@PathVariable Long id) {

        Map<String, Object> stockOut = jdbc.queryForMap("""
            SELECT 
                id,
                stock_out_code,
                exception_note,
                status
            FROM stock_outs
            WHERE id = ?
        """, id);

        List<Map<String, Object>> items = jdbc.queryForList("""
            SELECT 
                id,
                title_snapshot,
                is_missing_reported
            FROM stock_out_items
            WHERE stock_out_id = ?
              AND deleted_at IS NULL
        """, id);

        return Map.of(
                "stockOut", stockOut,
                "items", items
        );
    }
}
