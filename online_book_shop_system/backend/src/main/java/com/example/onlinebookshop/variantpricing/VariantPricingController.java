package com.example.onlinebookshop.variantpricing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management/variants")
@Tag(name = "3. Variant & Pricing")
public class VariantPricingController {

    private final VariantPricingService service;

    public VariantPricingController(VariantPricingService service) {
        this.service = service;
    }

    /* ═══ VARIANT CRUD ═══ */

    @GetMapping
    @Operation(summary = "List variants", description = "Get all variants, optionally filter by bookId")
    public List<VariantDTO> getAllVariants(@RequestParam(name = "bookId", required = false) Long bookId) {
        if (bookId != null)
            return service.getVariantsByBook(bookId);
        return service.getAllVariants();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get variant detail")
    public VariantDTO getVariant(@PathVariable("id") Long id) {
        return service.getVariantById(id);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get variant detail by SKU")
    public VariantDTO getVariantBySku(@PathVariable("sku") String sku) {
        return service.getVariantBySku(sku);
    }

    @PostMapping
    @Operation(summary = "Create variant", description = "Create a variant (cover type/edition/language) with SKU mapping")
    public ResponseEntity<VariantDTO> createVariant(@RequestBody CreateVariantRequest req) {
        VariantDTO created = service.createVariant(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update variant", description = "Update variant settings and inventory mapping")
    public VariantDTO updateVariant(@PathVariable("id") Long id, @RequestBody UpdateVariantRequest req) {
        return service.updateVariant(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete variant")
    public ResponseEntity<Map<String, String>> deleteVariant(@PathVariable("id") Long id) {
        service.deleteVariant(id);
        return ResponseEntity.ok(Map.of("message", "Variant deleted successfully"));
    }

    /* ═══ PRICING — BASE PRICE ═══ */

    @PutMapping("/{id}/price")
    @Operation(summary = "Set base price", description = "Set standard selling price (listPrice + salePrice) at variant level")
    public VariantDTO setBasePrice(@PathVariable("id") Long id, @RequestBody SetBasePriceRequest req) {
        return service.setBasePrice(id, req.getListPrice(), req.getSalePrice());
    }

    /* ═══ COPIES — LIST BY VARIANT ═══ */

    @GetMapping("/{variantId}/copies")
    @Operation(summary = "List copies for per-copy pricing", description = "Get all copies of a variant with their pricing info")
    public List<CopyPricingDTO> getCopiesByVariant(@PathVariable("variantId") Long variantId) {
        return service.getCopiesByVariant(variantId);
    }

    /* ═══ COPIES — OVERRIDE PRICE ═══ */

    @PutMapping("/copies/{copyId}/price-override")
    @Operation(summary = "Override price per copy", description = "Set a custom selling price for an individual copy. Pass null to clear.")
    public ResponseEntity<Map<String, String>> overrideCopyPrice(@PathVariable("copyId") Long copyId,
                                                                 @RequestBody OverrideCopyPriceRequest req) {
        service.overrideCopyPrice(copyId, req.getSellPriceOverride());
        return ResponseEntity.ok(Map.of("message", "Price override saved"));
    }
}
