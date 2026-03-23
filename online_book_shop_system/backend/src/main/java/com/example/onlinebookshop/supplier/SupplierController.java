package com.example.onlinebookshop.supplier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management/suppliers")
@CrossOrigin(origins = "*")
@Tag(name = "4. Supplier Management")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all suppliers", description = "Get all suppliers with purchase history summary")
    public List<SupplierDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public SupplierDTO getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create supplier", description = "Create a new supplier record")
    public ResponseEntity<SupplierDTO> create(@Valid @RequestBody CreateSupplierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier")
    public SupplierDTO update(@PathVariable("id") Long id, @Valid @RequestBody UpdateSupplierRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete supplier")
    public ResponseEntity<Map<String, String>> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Supplier deleted successfully"));
    }
}

