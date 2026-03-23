package com.example.onlinebookshop.report;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository repo;

    public ReportService(ReportRepository repo) {
        this.repo = repo;
    }

    public List<SalesReportDTO> getSalesByDay() {
        return repo.getSalesByDay();
    }

    public List<SalesReportDTO> getSalesByMonth() {
        return repo.getSalesByMonth();
    }

    public List<TopSellingDTO> getTopSelling(int limit) {
        return repo.getTopSelling(limit);
    }

    public List<SlowMoverDTO> getSlowMovers() {
        return repo.getSlowMovers();
    }

    public List<LotAgingDTO> getLotAging() {
        return repo.getLotAging();
    }

    public List<InventoryValueDTO> getInventoryValue() {
        return repo.getInventoryValue();
    }

    public List<ShrinkageDTO> getShrinkage() {
        return repo.getShrinkage();
    }

    public DashboardSummaryDTO getDashboardSummary() {
        return repo.getDashboardSummary();
    }
}

