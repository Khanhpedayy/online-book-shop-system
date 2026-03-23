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

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• VARIANT CRUD â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

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
            throw new IllegalArgumentException("Thiếu thông tin sách (bookId)");
        if (req.getSku() == null || req.getSku().isBlank())
            throw new IllegalArgumentException("Thiếu mã SKU");
        if (req.getListPrice() == null)
            throw new IllegalArgumentException("Thiếu Giá niêm yết (list_price)");
        if (req.getSalePrice() == null)
            throw new IllegalArgumentException("Thiếu Giá bán (sale_price)");
        Long id = repo.insertVariant(req);
        return repo.findVariantById(id);
    }

    @Transactional
    public VariantDTO updateVariant(Long id, UpdateVariantRequest req) {
        if (req.getListPrice() == null)
            throw new IllegalArgumentException("Thiếu Giá niêm yết (list_price)");
        if (req.getSalePrice() == null)
            throw new IllegalArgumentException("Thiếu Giá bán (sale_price)");
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

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• PRICING â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

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

    @Transactional
    public VariantDTO setConditionPrices(Long variantId, String conditionPricesJson) {
        getVariantById(variantId);
        repo.setConditionPrices(variantId, conditionPricesJson);
        return repo.findVariantById(variantId);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• PER-COPY PRICING â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

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

