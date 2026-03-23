package com.example.onlinebookshop.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "11. Reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/sales/daily")
    @Operation(summary = "Sales report â€” daily", description = "Revenue, order count, items sold grouped by day")
    public List<SalesReportDTO> getSalesByDay() {
        return service.getSalesByDay();
    }

    @GetMapping("/sales/monthly")
    @Operation(summary = "Sales report â€” monthly", description = "Revenue, order count, items sold grouped by month")
    public List<SalesReportDTO> getSalesByMonth() {
        return service.getSalesByMonth();
    }

    @GetMapping("/sales/top-selling")
    @Operation(summary = "Top selling titles", description = "Top N best-selling books by quantity")
    public List<TopSellingDTO> getTopSelling(@RequestParam(defaultValue = "20") int limit) {
        return service.getTopSelling(limit);
    }

    @GetMapping("/slow-movers")
    @Operation(summary = "Slow movers", description = "Titles with low sales velocity in last 30 days")
    public List<SlowMoverDTO> getSlowMovers() {
        return service.getSlowMovers();
    }

    @GetMapping("/lot-aging")
    @Operation(summary = "Lot aging report", description = "Lot age buckets (0-30 / 31-60 / 61-90 / 90+) with cost value")
    public List<LotAgingDTO> getLotAging() {
        return service.getLotAging();
    }

    @GetMapping("/inventory-value")
    @Operation(summary = "Inventory value by lot cost", description = "Inventory valuation based on average lot cost per variant")
    public List<InventoryValueDTO> getInventoryValue() {
        return service.getInventoryValue();
    }

    @GetMapping("/shrinkage")
    @Operation(summary = "Shrinkage report", description = "Loss/damage/adjustment breakdown by reason (DAMAGED / LOST / COUNT_DIFF)")
    public List<ShrinkageDTO> getShrinkage() {
        return service.getShrinkage();
    }

    @GetMapping("/summary")
    @Operation(summary = "Dashboard summary", description = "Overview: total books, variants, copies, inventory value, orders, revenue, alerts")
    public DashboardSummaryDTO getSummary() {
        return service.getDashboardSummary();
    }
}

