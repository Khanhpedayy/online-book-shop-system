package com.example.onlinebookshop.returnsdecision;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/returns")
@CrossOrigin(origins = "*")
@Tag(name = "10. Returns Decision")
public class ManagerReturnsDecisionController {

    private final ManagerReturnsDecisionService service;

    public ManagerReturnsDecisionController(ManagerReturnsDecisionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List returns", description = "List all returns with items and their current status (process action)")
    public List<ReturnOverviewDTO> getAll() {
        return service.getAllReturns();
    }

    @PutMapping("/items/{itemId}/process")
    @Operation(summary = "Process return item", description = "Process a return item: RESTOCK / RESTOCK_REPRICE / DAMAGED / SUPPLIER_RETURN")
    public ResponseEntity<Map<String, String>> processItem(
            @PathVariable Long itemId, @RequestBody ProcessReturnItemRequest req) {
        service.processReturnItem(itemId, req);
        return ResponseEntity.ok(Map.of("message", "Return item processed: " + req.getAction()));
    }
}

