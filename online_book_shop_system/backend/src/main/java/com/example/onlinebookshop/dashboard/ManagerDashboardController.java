package com.example.onlinebookshop.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "10. Manager Dashboard")
public class ManagerDashboardController {

    private final ManagerDashboardService service;

    public ManagerDashboardController(ManagerDashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get dashboard overview", description = "Returns aggregated stats for books, categories, and stock")
    public ManagerDashboardDTO getDashboard() {
        return service.getDashboard();
    }
}
