package com.example.onlinebookshop.payos;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.OrderService;
import com.example.onlinebookshop.dto.PayOSReturnSyncRequest;
import com.example.onlinebookshop.paymentlog.PaymentLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payos")
public class PayOSReturnSyncController {

    private final OrderService orderService;
    private final PaymentLogRepository paymentLogRepository;
    private final PayOsPaymentSyncService payOsPaymentSyncService;

    public PayOSReturnSyncController(OrderService orderService,
                                     PaymentLogRepository paymentLogRepository,
                                     PayOsPaymentSyncService payOsPaymentSyncService) {
        this.orderService = orderService;
        this.paymentLogRepository = paymentLogRepository;
        this.payOsPaymentSyncService = payOsPaymentSyncService;
    }

    /**
     * Fallback when webhook is not reachable (e.g. local testing).
     * Updates `orders.payment_status` using paymentLinkId from the return URL.
     */
    @PostMapping("/orders/{orderId}/sync-return")
    public ResponseEntity<Map<String, Object>> syncReturn(
            @PathVariable Long orderId,
            @RequestBody PayOSReturnSyncRequest request,
            Authentication auth
    ) {
        Order order = orderService.getOrderDetailByEmail(orderId, auth.getName());

        if (order.getPaymentMethod() == null || !"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new IllegalStateException("Sync is only available for PayOS orders.");
        }

        String paymentLinkId = request.getPaymentLinkId();
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            throw new IllegalArgumentException("paymentLinkId is required");
        }

        if (!paymentLogRepository.existsPayosPaymentLogForOrder(orderId, paymentLinkId)) {
            throw new IllegalStateException("Payment log not found for this order/paymentLinkId.");
        }

        String target = request.getTargetStatus();
        String normalized = "PAID".equalsIgnoreCase(target) ? "PAID" : "UNPAID";

        payOsPaymentSyncService.syncPaymentStatusByPaymentLinkId(paymentLinkId, normalized);

        Order updated = orderService.getOrderDetailByEmail(orderId, auth.getName());
        return ResponseEntity.ok(Map.of("paymentStatus", updated.getPaymentStatus()));
    }
}

