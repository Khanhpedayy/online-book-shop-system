package com.example.onlinebookshop.Controller;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.ShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/orders")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * View shipping order list — Lấy danh sách đơn cần xử lý (CONFIRMED, PACKED,
     * SHIPPED)
     */
    @GetMapping("/shipping")
    public List<Order> getShippingOrders() {
        return shippingService.getShippingOrders();
    }

    /**
     * Lấy tất cả đơn hàng (Admin tổng quan)
     */
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return shippingService.getAllOrders();
    }

    /**
     * Pick & Pack items — Staff đóng gói đơn
     */
    @PostMapping("/{id}/pack")
    public ResponseEntity<?> packOrder(@PathVariable Long id) {
        try {
            Order order = shippingService.packOrder(id);
            return ResponseEntity.ok(Map.of("message", "Đã đóng gói đơn hàng " + order.getOrderCode(),
                    "status", order.getStatus()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create shipping label — Staff nhận đơn đi giao.
     * Body: { "carrier": "Tên nhân viên" }
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String carrier = body.getOrDefault("carrier", "Nhân viên");
            Order order = shippingService.shipOrder(id, carrier);
            return ResponseEntity
                    .ok(Map.of("message", "Đơn hàng " + order.getOrderCode() + " đang được giao bởi " + carrier,
                            "status", order.getStatus()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delivery successful — Giao hàng thành công
     */
    @PostMapping("/{id}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long id) {
        try {
            Order order = shippingService.deliverOrder(id);
            return ResponseEntity.ok(Map.of("message", "Đơn hàng " + order.getOrderCode() + " đã giao thành công",
                    "status", order.getStatus()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delivery failed — Giao hàng thất bại
     * Body: { "reason": "Lý do thất bại" } (tuỳ chọn)
     */
    @PostMapping("/{id}/fail")
    public ResponseEntity<?> failDelivery(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = (body != null) ? body.getOrDefault("reason", "Giao hàng thất bại") : "Giao hàng thất bại";
            Order order = shippingService.failDelivery(id, reason);
            return ResponseEntity.ok(Map.of("message", "Đơn hàng " + order.getOrderCode() + " giao thất bại",
                    "status", order.getStatus()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
