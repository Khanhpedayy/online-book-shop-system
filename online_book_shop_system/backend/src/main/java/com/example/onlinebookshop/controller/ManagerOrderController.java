package com.example.onlinebookshop.manager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/orders")
public class ManagerOrderController {

    private final JdbcTemplate jdbc;

    public ManagerOrderController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * GET: danh sách đơn NEW
     */
    @GetMapping
    public List<Map<String, Object>> getOrders() {
        return jdbc.queryForList("""
            SELECT 
                id,
                order_code,
                ship_name,
                ship_phone,
                total_amount,
                status,
                payment_status,
                placed_at
            FROM orders
            WHERE status = 'NEW'
            ORDER BY placed_at DESC
        """);
    }

    /**
     * GET: chi tiết đơn
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {

        // thông tin đơn
        Map<String, Object> order = jdbc.queryForMap("""
            SELECT 
                id,
                order_code,
                ship_name,
                ship_phone,
                ship_line1,
                ship_line2,
                ship_ward,
                ship_district,
                ship_city,
                total_amount,
                status
            FROM orders
            WHERE id = ?
        """, id);

        // items
        List<Map<String, Object>> items = jdbc.queryForList("""
            SELECT 
                oi.id,
                oi.quantity,
                oi.unit_price,
                oi.line_total,
                oi.title_snapshot
            FROM order_items oi
            WHERE oi.order_id = ?
        """, id);

        return Map.of(
                "order", order,
                "items", items
        );
    }

    /**
     * PUT: confirm đơn
     */
    @PutMapping("/{id}/confirm")
    public Map<String, Object> confirmOrder(@PathVariable Long id) {

        int updated = jdbc.update("""
            UPDATE orders
            SET 
                status = 'CONFIRMED',
                confirmed_at = SYSUTCDATETIME()
            WHERE id = ? AND status = 'NEW'
        """, id);

        if (updated == 0) {
            throw new RuntimeException("Order not found or not in NEW status");
        }

        return Map.of(
                "success", true,
                "message", "Order confirmed"
        );
    }

    /**
     * PUT: cancel đơn (optional)
     */
    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {

        int updated = jdbc.update("""
            UPDATE orders
            SET 
                status = 'CANCELLED',
                cancel_reason = ?,
                cancelled_at = SYSUTCDATETIME()
            WHERE id = ? AND status = 'NEW'
        """, reason, id);

        if (updated == 0) {
            throw new RuntimeException("Order not found or cannot cancel");
        }

        return Map.of(
                "success", true
        );
    }
}