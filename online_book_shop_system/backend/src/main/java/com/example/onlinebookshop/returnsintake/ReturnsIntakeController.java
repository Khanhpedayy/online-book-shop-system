package com.example.onlinebookshop.returnsintake;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/returns/intake")
@Tag(name = "17. Returns Intake")
public class ReturnsIntakeController {
    private final ReturnsIntakeService service;

    public ReturnsIntakeController(ReturnsIntakeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List returns", description = "List all return intakes")
    public List<ReturnIntakeDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    @Operation(summary = "Create return intake", description = "Create a return intake record linked to an order")
    public ResponseEntity<Map<String, Long>> create(@RequestBody CreateReturnIntakeRequest req) {
        Long id = service.createIntake(req);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PostMapping("/{id}/scan")
    @Operation(summary = "Scan returned copy", description = "Scan copyCode for returned items")
    public ResponseEntity<Void> scan(@PathVariable("id") Long id, @RequestBody ScanReturnCopyRequest req) {
        service.scanCopy(id, req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/items/{itemId}/condition")
    @Operation(summary = "Record condition", description = "Record the actual condition on return")
    public ResponseEntity<Void> recordCondition(@PathVariable("id") Long id, @PathVariable("itemId") Long itemId,
            @RequestBody RecordConditionRequest req) {
        try {
            service.recordCondition(itemId, req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/escalate")
    @Operation(summary = "Escalate to manager", description = "Hand off to manager for restock/reprice/damaged decision")
    public ResponseEntity<Void> escalate(@PathVariable("id") Long id) {
        try {
            service.escalate(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

