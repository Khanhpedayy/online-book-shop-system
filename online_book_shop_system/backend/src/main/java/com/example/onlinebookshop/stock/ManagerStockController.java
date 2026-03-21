package com.example.onlinebookshop.stock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/stock")
@CrossOrigin(origins = "*")
@Tag(name = "9. Stock Management")
public class ManagerStockController {

    private final ManagerStockService service;

    public ManagerStockController(ManagerStockService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all books with stock", description = "View stock quantities of all books")
    public List<StockItemDTO> getAll() {
        return service.getAllStock();
    }

    @GetMapping("/adjustments")
    @Operation(summary = "List all stock adjustments", description = "View history of all stock changes")
    public List<StockAdjustmentDTO> getAllAdjustments(
            @RequestParam(required = false) Long bookId) {
        return service.getAdjustments(bookId);
    }

    @PutMapping("/{bookId}")
    @Operation(summary = "Set stock quantity", description = "Directly set the stock quantity for a book")
    public StockItemDTO setStock(@PathVariable Long bookId, @RequestBody UpdateStockRequest req) {
        return service.setStock(bookId, req);
    }

    @PostMapping("/{bookId}/adjust")
    @Operation(summary = "Adjust stock", description = "Import/Export stock with reason tracking")
    public StockItemDTO adjustStock(@PathVariable Long bookId, @RequestBody AdjustStockRequest req) {
        return service.adjustStock(bookId, req);
    }
}
