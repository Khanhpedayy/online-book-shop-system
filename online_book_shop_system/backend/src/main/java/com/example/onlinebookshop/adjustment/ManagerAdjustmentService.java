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
        if (req.getQuantity() <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero");
        if (req.getType() == null || req.getType().isBlank())
            throw new IllegalArgumentException("Type is required");

        Long id = repo.insert(req);
        // Update lot quantities based on direction and type
        if (req.getLotId() != null) {
            int signedQty = "OUT".equals(req.getDirection()) ? -Math.abs(req.getQuantity()) : Math.abs(req.getQuantity());
            repo.updateLotQtyAvailable(req.getLotId(), signedQty);
            if ("DAMAGE".equals(req.getType()) && signedQty < 0) {
                // If damaged and moving out, increase qtyDamaged in lot
                repo.updateLotQtyDamaged(req.getLotId(), Math.abs(signedQty));
            }
        }
        return id;
    }
}

