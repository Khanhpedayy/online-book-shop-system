package com.example.onlinebookshop.staffdashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("staffDashboardApiController")
@RequestMapping("/api/staff/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "12. Staff Dashboard")
public class StaffDashboardController {

    private final StaffDashboardService service;

    public StaffDashboardController(StaffDashboardService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    @Operation(summary = "View KPIs", description = "New orders, pending payments, orders to pack, shipped today")
    public StaffKpiDTO getKpis() {
        return service.getKpis();
    }

    @GetMapping("/todo")
    @Operation(summary = "View to-do list", description = "Prioritized tasks/orders based on SLA/priority")
    public List<TodoItemDTO> getTodoList() {
        return service.getTodoList();
    }

    @GetMapping("/alerts")
    @Operation(summary = "View alerts", description = "Overdue orders, payment mismatches, stock issues")
    public List<AlertDTO> getAlerts() {
        return service.getAlerts();
    }
}

