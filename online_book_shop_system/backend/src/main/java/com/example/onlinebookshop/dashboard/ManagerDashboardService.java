package com.example.onlinebookshop.dashboard;

import org.springframework.stereotype.Service;

@Service
public class ManagerDashboardService {

    private final ManagerDashboardRepository repo;

    public ManagerDashboardService(ManagerDashboardRepository repo) {
        this.repo = repo;
    }

    public ManagerDashboardDTO getDashboard() {
        ManagerDashboardDTO dto = new ManagerDashboardDTO();

        // Books
        dto.setTotalBooks(repo.countBooks());
        dto.setActiveBooks(repo.countBooksByStatus("ACTIVE"));
        dto.setDraftBooks(repo.countBooksByStatus("DRAFT"));

        // Categories
        dto.setTotalCategories(repo.countCategories());
        dto.setActiveCategories(repo.countActiveCategories());

        // Stock
        dto.setTotalStockQuantity(repo.sumStockQuantity());
        dto.setOutOfStockBooks(repo.countOutOfStock());
        dto.setLowStockBooks(repo.countLowStock());

        // Lists
        dto.setRecentBooks(repo.findRecentBooks(5));
        dto.setStockAlerts(repo.findStockAlerts());
        dto.setCategoryStats(repo.getCategoryStats());

        return dto;
    }
}
