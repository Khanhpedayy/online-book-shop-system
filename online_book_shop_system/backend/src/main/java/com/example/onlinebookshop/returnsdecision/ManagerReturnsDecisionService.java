package com.example.onlinebookshop.returnsdecision;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManagerReturnsDecisionService {

    private final ManagerReturnsDecisionRepository repo;

    public ManagerReturnsDecisionService(ManagerReturnsDecisionRepository repo) {
        this.repo = repo;
    }

    public List<ReturnOverviewDTO> getAllReturns() {
        return repo.findAllReturns();
    }

    @Transactional
    public void processReturnItem(Long itemId, ProcessReturnItemRequest req) {
        // Validate action
        if (req.getAction() == null || req.getAction().isBlank())
            throw new IllegalArgumentException("Action is required");
        String validActions = "RESTOCK,RESTOCK_REPRICE,DAMAGED,SUPPLIER_RETURN";
        if (!validActions.contains(req.getAction()))
            throw new IllegalArgumentException(
                    "Invalid action: " + req.getAction() + ". Must be one of: " + validActions);

        if ("RESTOCK_REPRICE".equals(req.getAction()) && req.getNewSellPrice() == null)
            throw new IllegalArgumentException("New sell price is required for RESTOCK_REPRICE action");

        // Find the specific item
        ReturnItemDTO item = null;
        List<ReturnOverviewDTO> all = repo.findAllReturns();
        for (ReturnOverviewDTO ret : all) {
            for (ReturnItemDTO i : ret.getItems()) {
                if (i.getId().equals(itemId)) {
                    item = i;
                    break;
                }
            }
            if (item != null)
                break;
        }

        if (item == null)
            throw new RuntimeException("Return item not found: " + itemId);

        if (item.getAction() != null && !item.getAction().isBlank())
            throw new IllegalArgumentException("Return item already processed with action: " + item.getAction());

        // Update the return_item action
        repo.processReturnItem(itemId, req.getAction());

        // Process based on action
        Long copyId = item.getCopyId();
        if (copyId != null) {
            switch (req.getAction()) {
                case "RESTOCK":
                    repo.updateCopyStatus(copyId, "AVAILABLE");
                    repo.logTransaction("RETURN", null, null, copyId, 1, "RETURN", itemId, "SALE",
                            "Restock: " + req.getNote());
                    break;
                case "RESTOCK_REPRICE":
                    repo.updateCopyStatus(copyId, "AVAILABLE");
                    if (req.getConditionGrade() != null) {
                        repo.updateCopyCondition(copyId, req.getConditionGrade());
                    }
                    if (req.getNewSellPrice() != null) {
                        repo.updateCopyPriceOverride(copyId, req.getNewSellPrice());
                    }
                    repo.logTransaction("RETURN", null, null, copyId, 1, "RETURN", itemId, "SALE",
                            "Restock+Reprice to " + req.getConditionGrade() + ": " + req.getNote());
                    break;
                case "DAMAGED":
                    repo.updateCopyStatus(copyId, "DAMAGED");
                    repo.logTransaction("ADJUST", null, null, copyId, 1, "RETURN", itemId, "DAMAGED", req.getNote());
                    break;
                case "SUPPLIER_RETURN":
                    repo.updateCopyStatus(copyId, "RETURNED");
                    repo.logTransaction("OUT", null, null, copyId, 1, "RETURN", itemId, "SALE",
                            "Supplier return: " + req.getNote());
                    break;
            }
        }
    }
}

