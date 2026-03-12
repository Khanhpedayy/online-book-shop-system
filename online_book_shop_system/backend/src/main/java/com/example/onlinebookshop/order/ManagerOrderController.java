package com.example.onlinebookshop.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
@CrossOrigin(origins = "*")
@Tag(name = "11. Staff Orders")
public class ManagerOrderController {

    private final ManagerOrderService service;

    public ManagerOrderController(ManagerOrderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List orders", description = "List orders with filters: status, paymentStatus, deliveryStatus, search, sortBy, sortDir")
    public List<OrderListDTO> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return service.getAll(status, paymentStatus, deliveryStatus, search, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Order detail", description = "Get order detail with items, customer info, and internal notes")
    public ResponseEntity<OrderDetailDTO> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/notes")
    @Operation(summary = "Add internal note", description = "Add an internal staff note to an order")
    public ResponseEntity<Void> addNote(@PathVariable Long id, @RequestBody AddNoteRequest req) {
        service.addNote(id, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm order", description = "Confirm/accept an order for processing")
    public ResponseEntity<Void> confirmOrder(@PathVariable Long id) {
        try {
            service.confirmOrder(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

