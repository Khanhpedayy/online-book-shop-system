package com.example.onlinebookshop.inventory;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryOverviewService {

    private final InventoryOverviewRepository repository;

    public InventoryOverviewService(InventoryOverviewRepository repository) {
        this.repository = repository;
    }

    public List<StockByVariantDTO> getStockByVariant() {
        return repository.getStockByVariant();
    }

    public List<StockByLotDTO> getStockByLot() {
        return repository.getStockByLot();
    }

    public List<StockByConditionDTO> getStockByCondition() {
        return repository.getStockByCondition();
    }

    public List<LowStockAlertDTO> getLowStockAlerts() {
        return repository.getLowStockAlerts(20); // Hardcoded threshold for now, can be changed via config later
    }

    public List<OverstockAlertDTO> getOverstockAgingAlerts() {
        return repository.getAgingLotAlerts(90); // 90 days considered aging/slow-moving
    }
}

