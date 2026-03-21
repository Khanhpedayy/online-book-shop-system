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
@CrossOrigin(origins = "*")
@Tag(name = "3. Variant & Pricing")
public class ManagerVariantPricingController {

    private final ManagerVariantPricingService service;

    public ManagerVariantPricingController(ManagerVariantPricingService service) {
        this.service = service;
    }

    /* â•â•â• VARIANT CRUD â•â•â• */

    @GetMapping
    @Operation(summary = "List variants", description = "Get all variants, optionally filter by bookId")
    public List<VariantDTO> getAllVariants(@RequestParam(required = false) Long bookId) {
        if (bookId != null)
            return service.getVariantsByBook(bookId);
        return service.getAllVariants();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get variant detail")
    public VariantDTO getVariant(@PathVariable Long id) {
        return service.getVariantById(id);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get variant detail by SKU")
    public VariantDTO getVariantBySku(@PathVariable String sku) {
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
    public VariantDTO updateVariant(@PathVariable Long id, @RequestBody UpdateVariantRequest req) {
        return service.updateVariant(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete variant")
    public ResponseEntity<Map<String, String>> deleteVariant(@PathVariable Long id) {
        service.deleteVariant(id);
        return ResponseEntity.ok(Map.of("message", "Variant deleted successfully"));
    }

    /* â•â•â• PRICING â€” BASE PRICE â•â•â• */

    @PutMapping("/{id}/price")
    @Operation(summary = "Set base price", description = "Set standard selling price (listPrice + salePrice) at variant level")
    public VariantDTO setBasePrice(@PathVariable Long id, @RequestBody SetBasePriceRequest req) {
        return service.setBasePrice(id, req.getListPrice(), req.getSalePrice());
    }

    /* â•â•â• PRICING â€” CONDITION PRICES â•â•â• */

    @PutMapping("/{id}/condition-prices")
    @Operation(summary = "Set price by condition", description = "Set pricing rules by condition tier (LIKE_NEW / GOOD / FAIR) as JSON")
    public VariantDTO setConditionPrices(@PathVariable Long id, @RequestBody SetConditionPricesRequest req) {
        return service.setConditionPrices(id, req.getConditionPricesJson());
    }

    /* â•â•â• COPIES â€” LIST BY VARIANT (for per-copy pricing) â•â•â• */

    @GetMapping("/{variantId}/copies")
    @Operation(summary = "List copies for per-copy pricing", description = "Get all copies of a variant with their pricing info")
    public List<CopyPricingDTO> getCopiesByVariant(@PathVariable Long variantId) {
        return service.getCopiesByVariant(variantId);
    }

    /* â•â•â• COPIES â€” OVERRIDE PRICE â•â•â• */

    @PutMapping("/copies/{copyId}/price-override")
    @Operation(summary = "Override price per copy", description = "Set a custom selling price for an individual copy. Pass null to clear.")
    public ResponseEntity<Map<String, String>> overrideCopyPrice(@PathVariable Long copyId,
                                                                 @RequestBody OverrideCopyPriceRequest req) {
        service.overrideCopyPrice(copyId, req.getSellPriceOverride());
        return ResponseEntity.ok(Map.of("message", "Price override saved"));
    }
}

