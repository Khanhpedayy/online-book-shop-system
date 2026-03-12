package com.example.onlinebookshop.adjustment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManagerAdjustmentService {

    private final ManagerAdjustmentRepository repo;

    public ManagerAdjustmentService(ManagerAdjustmentRepository repo) {
        this.repo = repo;
    }

    public List<AdjustmentDTO> getAll() {
        return repo.findAll();
    }

    @Transactional
    public Long createAdjustment(CreateAdjustmentRequest req) {
        // Validate required fields
        if (req.getVariantId() == null)
            throw new IllegalArgumentException("Variant is required");
        if (req.getQuantity() == 0)
            throw new IllegalArgumentException("Quantity must not be zero");
        if (req.getReason() == null || req.getReason().isBlank())
            throw new IllegalArgumentException("Reason is required");
        String validReasons = "DAMAGED,LOST,FOUND,COUNT_DIFF,TRANSFER";
        if (!validReasons.contains(req.getReason()))
            throw new IllegalArgumentException(
                    "Invalid reason: " + req.getReason() + ". Must be one of: " + validReasons);

        Long id = repo.insert(req);
        // Update lot quantities based on reason
        if (req.getLotId() != null) {
            if ("DAMAGED".equals(req.getReason()) || "LOST".equals(req.getReason())) {
                repo.updateLotQtyAvailable(req.getLotId(), -Math.abs(req.getQuantity()));
                if ("DAMAGED".equals(req.getReason())) {
                    repo.updateLotQtyDamaged(req.getLotId(), Math.abs(req.getQuantity()));
                }
            } else if ("FOUND".equals(req.getReason())) {
                repo.updateLotQtyAvailable(req.getLotId(), Math.abs(req.getQuantity()));
            } else if ("COUNT_DIFF".equals(req.getReason())) {
                repo.updateLotQtyAvailable(req.getLotId(), req.getQuantity());
            }
        }
        return id;
    }
}

