package com.example.onlinebookshop.variantpricing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariantPricingService {

    private final VariantPricingRepository repo;

    public VariantPricingService(VariantPricingRepository repo) {
        this.repo = repo;
    }

    /* ═══════════════════════ VARIANT CRUD ═══════════════════════ */

    public List<VariantDTO> getVariantsByBook(Long bookId) {
        return repo.findVariantsByBookId(bookId);
    }

    public List<VariantDTO> getAllVariants() {
        return repo.findAllVariants();
    }

    public VariantDTO getVariantById(Long id) {
        VariantDTO v = repo.findVariantById(id);
        if (v == null)
            throw new RuntimeException("Variant not found with id: " + id);
        return v;
    }

    public VariantDTO getVariantBySku(String sku) {
        VariantDTO v = repo.findVariantBySku(sku);
        if (v == null)
            throw new RuntimeException("Variant not found with SKU: " + sku);
        return v;
    }

    @Transactional
    public VariantDTO createVariant(CreateVariantRequest req) {
        if (req.getBookId() == null)
            throw new IllegalArgumentException("Thi\u1ebfu th\u00f4ng tin s\u00e1ch (bookId)");
        if (req.getSku() == null || req.getSku().isBlank())
            throw new IllegalArgumentException("Thi\u1ebfu m\u00e3 SKU");
        if (req.getListPrice() == null)
            throw new IllegalArgumentException("Thi\u1ebfu Gi\u00e1 ni\u00eam y\u1ebft (list_price)");
        if (req.getSalePrice() == null)
            throw new IllegalArgumentException("Thi\u1ebfu Gi\u00e1 b\u00e1n (sale_price)");
        Long id = repo.insertVariant(req);
        return repo.findVariantById(id);
    }

    @Transactional
    public VariantDTO updateVariant(Long id, UpdateVariantRequest req) {
        if (req.getListPrice() == null)
            throw new IllegalArgumentException("Thi\u1ebfu Gi\u00e1 ni\u00eam y\u1ebft (list_price)");
        if (req.getSalePrice() == null)
            throw new IllegalArgumentException("Thi\u1ebfu Gi\u00e1 b\u00e1n (sale_price)");
        getVariantById(id); // ensure exists
        int rows = repo.updateVariant(id, req);
        if (rows == 0)
            throw new RuntimeException("Failed to update variant");
        return repo.findVariantById(id);
    }

    public void deleteVariant(Long id) {
        int rows = repo.softDeleteVariant(id);
        if (rows == 0)
            throw new RuntimeException("Variant not found with id: " + id);
    }

    /* ═══════════════════════ PRICING ═══════════════════════ */

    @Transactional
    public VariantDTO setBasePrice(Long variantId, Double listPrice, Double salePrice) {
        getVariantById(variantId);
        if (listPrice == null || listPrice < 0)
            throw new IllegalArgumentException("listPrice must be >= 0");
        if (salePrice == null || salePrice < 0)
            throw new IllegalArgumentException("salePrice must be >= 0");
        repo.setBasePrice(variantId, listPrice, salePrice);
        return repo.findVariantById(variantId);
    }

    /* ═══════════════════════ PER-COPY PRICING ═══════════════════════ */

    public List<CopyPricingDTO> getCopiesByVariant(Long variantId) {
        return repo.findCopiesByVariantId(variantId);
    }

    @Transactional
    public void overrideCopyPrice(Long copyId, Double sellPriceOverride) {
        int rows = repo.overrideCopyPrice(copyId, sellPriceOverride);
        if (rows == 0)
            throw new RuntimeException("Copy not found with id: " + copyId);
    }
}
