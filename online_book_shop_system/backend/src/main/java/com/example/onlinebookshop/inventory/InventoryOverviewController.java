package com.example.onlinebookshop.inventory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/overview")
@Tag(name = "1. Inventory Overview")
public class InventoryOverviewController {

    private final InventoryOverviewService service;

    public InventoryOverviewController(InventoryOverviewService service) {
        this.service = service;
    }

    @GetMapping("/variants")
    @Operation(summary = "View stock by title/variant", description = "Aggregate available stock per book variant (title + SKU + format)")
    public List<StockByVariantDTO> getStockByVariant() {
        return service.getStockByVariant();
    }

    @GetMapping("/lots")
    @Operation(summary = "View stock by lot", description = "View inventory by inbound lot/batch")
    public List<StockByLotDTO> getStockByLot() {
        return service.getStockByLot();
    }

    @GetMapping("/conditions")
    @Operation(summary = "View stock by condition", description = "View inventory by condition tier (NEW / LIKE_NEW / GOOD / FAIR)")
    public List<StockByConditionDTO> getStockByCondition() {
        return service.getStockByCondition();
    }

    @GetMapping("/alerts/low-stock")
    @Operation(summary = "Low stock alerts", description = "Variants with total available â‰¤ threshold (default 20)")
    public List<LowStockAlertDTO> getLowStockAlerts() {
        return service.getLowStockAlerts();
    }

    @GetMapping("/alerts/overstock")
    @Operation(summary = "Overstock / aging alerts", description = "Lots with stock older than 90 days (slow-moving)")
    public List<OverstockAlertDTO> getOverstockAgingAlerts() {
        return service.getOverstockAgingAlerts();
    }
}

