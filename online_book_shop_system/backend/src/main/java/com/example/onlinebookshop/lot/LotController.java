package com.example.onlinebookshop.lot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/lots")
@CrossOrigin(origins = "*")
@Tag(name = "5. Lot / Goods Receipt")
public class LotController {

    private final LotService service;

    public LotController(LotService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "View lots", description = "List lots filtered by supplier, variant, received date, title")
    public List<LotDTO> getAll(
            @RequestParam(name = "supplierId", required = false) Long supplierId,
            @RequestParam(name = "variantId", required = false) Long variantId) {
        return service.getAll(supplierId, variantId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "View lot detail", description = "Lot stock breakdown + aging & cost + copies list")
    public LotDetailDTO getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create lot (goods receipt)", description = "Create a lot with lotCode, supplier, received date, unit cost, qty, invoice/note")
    public ResponseEntity<LotDTO> create(@RequestBody CreateLotRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLot(req));
    }

    @PostMapping("/{id}/generate-copies")
    @Operation(summary = "Generate copies for lot", description = "Generate individual copies under a lot for per-copy tracking")
    public ResponseEntity<Map<String, Object>> generateCopies(
            @PathVariable("id") Long id, @RequestBody GenerateCopiesRequest req) {
        int count = service.generateCopies(id, req);
        return ResponseEntity.ok(Map.of("generated", count, "message", "Generated " + count + " copies"));
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock lot", description = "Lock a lot to stop allocation/sales")
    public ResponseEntity<Map<String, String>> lock(@PathVariable("id") Long id,
                                                    @RequestBody(required = false) LockLotRequest req) {
        service.lockLot(id, req != null ? req.getReason() : null);
        return ResponseEntity.ok(Map.of("message", "Lot locked successfully"));
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock lot", description = "Unlock a lot to allow allocation/sales again")
    public ResponseEntity<Map<String, String>> unlock(@PathVariable("id") Long id) {
        service.unlockLot(id);
        return ResponseEntity.ok(Map.of("message", "Lot unlocked successfully"));
    }

    @GetMapping("/code/{lotCode}")
    @Operation(summary = "Get lot by code", description = "Find a specific lot by its lotCode instead of ID")
    public LotDetailDTO getByLotCode(@PathVariable("lotCode") String lotCode) {
        return service.getByLotCode(lotCode);
    }
}

