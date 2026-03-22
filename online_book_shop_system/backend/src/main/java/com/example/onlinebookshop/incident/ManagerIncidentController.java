package com.example.onlinebookshop.incident;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff/incidents")
@CrossOrigin(origins = "*")
@Tag(name = "19. Incidents")
public class ManagerIncidentController {
    private final ManagerIncidentService service;

    public ManagerIncidentController(ManagerIncidentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List incidents", description = "List all picking/inventory incidents")
    public List<IncidentDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    @Operation(summary = "Report incident", description = "Report missing/damaged items or picking errors")
    public ResponseEntity<Void> create(@RequestBody CreateIncidentRequest req) {
        service.create(req);
        return ResponseEntity.ok().build();
    }
}

