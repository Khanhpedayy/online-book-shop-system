package com.example.onlinebookshop.adjustment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdjustmentService {

    private final AdjustmentRepository repo;

    public AdjustmentService(AdjustmentRepository repo) {
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

        // Tự động xác định chiều dựa theo loại điều chỉnh (business rule)
        // FOUND (tìm thấy) = IN (nhập vào); DAMAGE/LOST = OUT (xuất ra)
        if (req.getDirection() == null || req.getDirection().isBlank()) {
            req.setDirection("FOUND".equalsIgnoreCase(req.getType()) ? "IN" : "OUT");
        }

        Long id = repo.insert(req);

        // Cập nhật số lượng lô
        if (req.getLotId() != null) {
            int signedQty = "OUT".equals(req.getDirection()) ? -Math.abs(req.getQuantity()) : Math.abs(req.getQuantity());
            repo.updateLotQtyAvailable(req.getLotId(), signedQty);

            if ("DAMAGE".equalsIgnoreCase(req.getType()) && signedQty < 0) {
                // Hỏng/xuất: tăng qty_damaged
                repo.updateLotQtyDamaged(req.getLotId(), Math.abs(signedQty));
            } else if ("FOUND".equalsIgnoreCase(req.getType()) && signedQty > 0) {
                // Tìm thấy lại: giảm qty_damaged (nếu có)
                repo.updateLotQtyDamaged(req.getLotId(), -Math.abs(signedQty));
            }
        }
        return id;
    }
}

