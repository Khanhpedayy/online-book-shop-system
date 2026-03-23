package com.example.onlinebookshop.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "10. Manager Dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get dashboard overview", description = "Returns aggregated stats for books, categories, and stock")
    public DashboardDTO getDashboard() {
        return service.getDashboard();
    }
}
