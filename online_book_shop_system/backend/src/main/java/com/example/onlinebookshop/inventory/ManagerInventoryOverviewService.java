package com.example.onlinebookshop.inventory;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ManagerInventoryOverviewService {

    private final ManagerInventoryOverviewRepository repository;

    public ManagerInventoryOverviewService(ManagerInventoryOverviewRepository repository) {
        this.repository = repository;
    }

    public List<ManagerStockByVariantDTO> getStockByVariant() {
        return repository.getStockByVariant();
    }

    public List<ManagerStockByLotDTO> getStockByLot() {
        return repository.getStockByLot();
    }

    public List<ManagerStockByConditionDTO> getStockByCondition() {
        return repository.getStockByCondition();
    }

    public List<ManagerLowStockAlertDTO> getLowStockAlerts() {
        return repository.getLowStockAlerts(20); // Hardcoded threshold for now, can be changed via config later
    }

    public List<ManagerOverstockAlertDTO> getOverstockAgingAlerts() {
        return repository.getAgingLotAlerts(90); // 90 days considered aging/slow-moving
    }
}

