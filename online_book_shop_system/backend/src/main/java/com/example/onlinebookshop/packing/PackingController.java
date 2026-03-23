package com.example.onlinebookshop.packing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/orders/{orderId}/packing")
@Tag(name = "14. Packing")
public class PackingController {

    private final PackingService service;

    public PackingController(PackingService service) {
        this.service = service;
    }

    @GetMapping("/status")
    @Operation(summary = "Packing status", description = "Check if all items picked, packing confirmed, etc.")
    public ResponseEntity<PackingStatusDTO> getStatus(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(service.getStatus(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slip")
    @Operation(summary = "Packing slip", description = "Get packing slip data for printing")
    public ResponseEntity<PackingSlipDTO> getSlip(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(service.getPackingSlip(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/mark-packed")
    @Operation(summary = "Mark packed", description = "Update order status to PACKED")
    public ResponseEntity<Void> markPacked(@PathVariable Long orderId) {
        try {
            service.markPacked(orderId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

