package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.staff.service.StaffNotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/orders")
public class ManagerOrderController {

    private final JdbcTemplate jdbc;
    private final StaffNotificationService notificationService;

    public ManagerOrderController(JdbcTemplate jdbc, StaffNotificationService notificationService) {
        this.jdbc = jdbc;
        this.notificationService = notificationService;
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
     * PUT: cancel đơn (với notification)
     */
    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        // 1. Lấy thông tin user và order_code trước khi cập nhật
        Map<String, Object> orderData;
        try {
            orderData = jdbc.queryForMap("SELECT user_id, order_code FROM orders WHERE id = ?", id);
        } catch (Exception e) {
            throw new RuntimeException("Order not found: " + id);
        }

        long userId = ((Number) orderData.get("user_id")).longValue();
        String orderCode = (String) orderData.get("order_code");

        // 2. Cập nhật trạng thái
        int updated = jdbc.update("""
            UPDATE orders
            SET 
                status = 'CANCELLED',
                cancel_reason = ?,
                cancelled_at = SYSUTCDATETIME()
            WHERE id = ? AND status = 'NEW'
        """, reason, id);

        if (updated == 0) {
            throw new RuntimeException("Order cannot be cancelled (maybe not in NEW status)");
        }

        // 3. Gửi thông báo cho khách hàng
        try {
            notificationService.notifyOrderCancelled(userId, orderCode, id, reason);
        } catch (Exception e) {
            // Log error but don't fail the request if notification fails
            System.err.println("Failed to send notification for cancelled order " + id + ": " + e.getMessage());
        }

        return Map.of(
                "success", true,
                "message", "Order cancelled and customer notified"
        );
    }

//    @GetMapping
//    public List<Map<String, Object>> getStockOuts() {
//        return jdbc.queryForList("""
//            SELECT
//                id,
//                stock_out_code,
//                exception_note
//            FROM stock_outs
//            WHERE has_exception = 1
//              AND deleted_at IS NULL
//            ORDER BY created_at DESC
//        """);
//    }
//
//    /**
//     * GET: chi tiết phiếu lỗi
//     */
//    @GetMapping("/{id}")
//    public Map<String, Object> getStockOutDetail(@PathVariable Long id) {
//
//        // header
//        Map<String, Object> stockOut = jdbc.queryForMap("""
//            SELECT
//                id,
//                stock_out_code,
//                exception_note,
//                status
//            FROM stock_outs
//            WHERE id = ?
//        """, id);
//
//        // items
//        List<Map<String, Object>> items = jdbc.queryForList("""
//            SELECT
//                id,
//                title_snapshot,
//                is_missing_reported
//            FROM stock_out_items
//            WHERE stock_out_id = ?
//              AND deleted_at IS NULL
//        """, id);
//
//        return Map.of(
//                "stockOut", stockOut,
//                "items", items
//        );
//    }
}