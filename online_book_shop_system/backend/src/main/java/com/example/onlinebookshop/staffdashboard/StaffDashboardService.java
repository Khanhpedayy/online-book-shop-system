package com.example.onlinebookshop.staffdashboard;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StaffDashboardService {

    private final StaffDashboardRepository repo;

    public StaffDashboardService(StaffDashboardRepository repo) {
        this.repo = repo;
    }

    public StaffKpiDTO getKpis() {
        return repo.getKpis();
    }

    public List<TodoItemDTO> getTodoList() {
        return repo.getTodoList();
    }

    public List<AlertDTO> getAlerts() {
        return repo.getAlerts();
    }
}