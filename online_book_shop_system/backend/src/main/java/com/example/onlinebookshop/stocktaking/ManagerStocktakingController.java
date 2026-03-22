package com.example.onlinebookshop.stocktaking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/stocktaking")
@CrossOrigin(origins = "*")
@Tag(name = "8. Cycle Count / Stocktaking")
public class ManagerStocktakingController {

    private final ManagerStocktakingService service;

    public ManagerStocktakingController(ManagerStocktakingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List stocktaking sessions")
    public List<StocktakingSessionDTO> getSessions() {
        return service.getAllSessions();
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get stocktaking session detail")
    public StocktakingSessionDTO getSession(@PathVariable String code) {
        return service.getSession(code);
    }

    @PostMapping
    @Operation(summary = "Create stocktaking session")
    public ResponseEntity<StocktakingSessionDTO> create(@RequestBody CreateStocktakingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSession(req));
    }

    @PostMapping("/{code}/count")
    @Operation(summary = "Record count for a variant/lot")
    public StocktakingSessionDTO recordCount(@PathVariable String code,
                                             @RequestBody RecordCountRequest req) {
        return service.recordCount(code, req);
    }

    @PostMapping("/{code}/apply")
    @Operation(summary = "Apply adjustments from stocktaking")
    public ResponseEntity<Map<String, Object>> apply(@PathVariable String code,
                                                     @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        StocktakingSessionDTO session = service.applyAdjustments(code, note);
        return ResponseEntity.ok(Map.of("message", "Stocktaking adjustments applied", "session", session));
    }
}

