package com.example.onlinebookshop.adjustment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/adjustments")
@CrossOrigin(origins = "*")
@Tag(name = "7. Inventory Adjustment")
public class AdjustmentController {

    private final AdjustmentService service;

    public AdjustmentController(AdjustmentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List adjustments", description = "Get all inventory adjustments with audit trail")
    public List<AdjustmentDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    @Operation(summary = "Adjust stock", description = "Create an inventory adjustment (damaged/lost/found/count-diff)")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateAdjustmentRequest req) {
        Long id = service.createAdjustment(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id, "message", "Adjustment created successfully"));
    }
}

