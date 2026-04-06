package com.example.onlinebookshop.lot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LotService {

    private final LotRepository repo;

    public LotService(LotRepository repo) {
        this.repo = repo;
    }

    public List<LotDTO> getAll(Long supplierId, Long variantId) {
        return repo.findAll(supplierId, variantId);
    }

    public LotDetailDTO getById(Long id) {
        LotDTO lot = repo.findById(id);
        if (lot == null)
            throw new RuntimeException("Lot not found: " + id);
        LotDetailDTO detail = new LotDetailDTO();
        detail.setId(lot.getId());
        detail.setLotCode(lot.getLotCode());
        detail.setSupplierId(lot.getSupplierId());
        detail.setSupplierName(lot.getSupplierName());
        detail.setVariantId(lot.getVariantId());
        detail.setVariantSku(lot.getVariantSku());
        detail.setBookTitle(lot.getBookTitle());
        detail.setReceiptCode(lot.getReceiptCode());
        detail.setInvoiceNo(lot.getInvoiceNo());
        detail.setWarehouse(lot.getWarehouse());
        detail.setReceivedAt(lot.getReceivedAt());
        detail.setUnitCost(lot.getUnitCost());
        detail.setQtyReceived(lot.getQtyReceived());
        detail.setQtyAvailable(lot.getQtyAvailable());
        detail.setQtyReserved(lot.getQtyReserved());
        detail.setQtySold(lot.getQtySold());
        detail.setQtyDamaged(lot.getQtyDamaged());
        detail.setQtyReturned(lot.getQtyReturned());
        detail.setConditionDefault(lot.getConditionDefault());
        detail.setStatus(lot.getStatus());
        detail.setNote(lot.getNote());
        detail.setCreatedAt(lot.getCreatedAt());
        detail.setAgeDays(lot.getAgeDays());
        detail.setTotalCostValue(lot.getTotalCostValue());
        detail.setCopies(repo.findCopiesByLot(id));
        return detail;
    }

    @Transactional
    public LotDTO createLot(CreateLotRequest req) {
        // Validate required fields
        if (req.getLotCode() == null || req.getLotCode().isBlank())
            throw new IllegalArgumentException("Lot code is required");
        if (req.getSupplierId() == null)
            throw new IllegalArgumentException("Supplier is required");
        if (req.getVariantId() == null)
            throw new IllegalArgumentException("Variant is required");
        if (req.getUnitCost() == null || req.getUnitCost() < 0)
            throw new IllegalArgumentException("Unit cost must be >= 0");
        if (req.getQtyReceived() <= 0)
            throw new IllegalArgumentException("Quantity received must be > 0");

        Long lotId = repo.insert(req);
        // Log IN transaction
        repo.logTransaction("IN", req.getVariantId(), lotId, null,
                req.getQtyReceived(), "RECEIPT", lotId, "SALE", req.getNote());
        return repo.findById(lotId);
    }

    @Transactional
    public int generateCopies(Long lotId, GenerateCopiesRequest req) {
        LotDTO lot = repo.findById(lotId);
        if (lot == null)
            throw new RuntimeException("Lot not found: " + lotId);
        if (lot.getQtyReceived() <= 0)
            throw new IllegalArgumentException("Lot has no quantity to generate copies");

        // Kiểm tra số bản sao đã tồn tại
        int existingCount = repo.countCopiesByLot(lotId);
        int remaining = lot.getQtyReceived() - existingCount;

        if (remaining <= 0)
            throw new IllegalArgumentException(
                "Lô hàng này đã có đủ " + lot.getQtyReceived() + " bản sao. Không thể tạo thêm.");

        String prefix = req.getPrefix() != null ? req.getPrefix() : lot.getLotCode() + "-";
        String condition = req.getConditionGrade() != null ? req.getConditionGrade() : lot.getConditionDefault();
        // Chỉ generate số bản sao còn thiếu (bắt đầu đánh số từ existingCount + 1)
        return repo.generateCopiesFrom(lotId, lot.getVariantId(), existingCount + 1,
                existingCount + remaining, prefix, req.getDefaultLocation(), condition);
    }

    @Transactional
    public void lockLot(Long id, String reason) {
        int rows = repo.lockLot(id);
        if (rows == 0)
            throw new RuntimeException("Lot not found: " + id);
        // Note: We don't log to inventory_transactions because quantity = 0 violates CK_it_qty
        // and locking a lot doesn't actually move physical inventory.
    }

    @Transactional
    public void unlockLot(Long id) {
        int rows = repo.unlockLot(id);
        if (rows == 0)
            throw new RuntimeException("Lot not found: " + id);
    }

    /* â”€â”€ Lookup: variants for dropdown â”€â”€ */
    public List<LotDTO> getLotsByVariant(Long variantId) {
        return repo.findAll(null, variantId);
    }

    public LotDetailDTO getByLotCode(String lotCode) {
        LotDTO lot = repo.findByLotCode(lotCode);
        if (lot == null)
            throw new RuntimeException("Lot not found: " + lotCode);
        return getById(lot.getId());
    }
}
