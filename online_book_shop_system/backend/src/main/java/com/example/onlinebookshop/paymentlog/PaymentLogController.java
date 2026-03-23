package com.example.onlinebookshop.paymentlog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "16. Payment Logs")
public class PaymentLogController {
    private final PaymentLogService service;

    public PaymentLogController(PaymentLogService service) {
        this.service = service;
    }

    @GetMapping("/api/staff/orders/{orderId}/payments")
    @Operation(summary = "Payment logs by order", description = "View PayOS payment logs/status for an order")
    public List<PaymentLogDTO> getByOrder(@PathVariable("orderId") Long orderId) {
        return service.getByOrderId(orderId);
    }

    @PutMapping("/api/staff/payments/{id}/flag")
    @Operation(summary = "Flag mismatch", description = "Flag suspicious payment case")
    public ResponseEntity<Void> flag(@PathVariable("id") Long id, @RequestBody FlagPaymentRequest req) {
        try {
            service.flagPayment(id, req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/staff/payments/{id}/recheck")
    @Operation(summary = "Trigger recheck", description = "Trigger a payment status recheck")
    public ResponseEntity<Map<String, String>> recheck(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(service.recheck(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

