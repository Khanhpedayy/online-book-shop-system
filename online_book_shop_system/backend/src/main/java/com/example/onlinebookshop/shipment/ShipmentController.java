package com.example.onlinebookshop.shipment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/orders/{orderId}/shipment")
@Tag(name = "15. Shipment")
public class ShipmentController {
    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get shipment", description = "Get shipment details for order")
    public ResponseEntity<ShipmentDTO> get(@PathVariable Long orderId) {
        ShipmentDTO s = service.getByOrderId(orderId);
        return s != null ? ResponseEntity.ok(s) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Create shipment", description = "Create shipment with carrier and tracking code")
    public ResponseEntity<Void> create(@PathVariable Long orderId, @RequestBody CreateShipmentRequest req) {
        service.create(orderId, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ship")
    @Operation(summary = "Mark shipped", description = "Update order status to shipped")
    public ResponseEntity<Void> markShipped(@PathVariable Long orderId) {
        try {
            service.markShipped(orderId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/deliver")
    @Operation(summary = "Update delivery", description = "Manually update delivered/failed outcome")
    public ResponseEntity<Void> updateDelivery(@PathVariable Long orderId, @RequestBody UpdateDeliveryRequest req) {
        try {
            service.updateDelivery(orderId, req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

