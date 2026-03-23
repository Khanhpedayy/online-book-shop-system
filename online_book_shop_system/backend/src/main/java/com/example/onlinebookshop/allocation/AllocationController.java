package com.example.onlinebookshop.allocation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/allocation")
@Tag(name = "9. Allocation Settings")
public class AllocationController {

    private final AllocationService service;

    public AllocationController(AllocationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get allocation settings", description = "Get FIFO rules, reservation TTL, condition priority, staff override policy")
    public AllocationSettingsDTO getSettings() {
        return service.getSettings();
    }

    @PutMapping
    @Operation(summary = "Update allocation settings", description = "Configure FIFO (LOT/COPY), reservation TTL (minutes), condition priority, allow staff override")
    public ResponseEntity<AllocationSettingsDTO> updateSettings(@RequestBody AllocationSettingsDTO dto) {
        return ResponseEntity.ok(service.updateSettings(dto));
    }
}

