package com.example.onlinebookshop.copy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/copies")
@CrossOrigin(origins = "*")
@Tag(name = "6. Copy Registry")
public class ManagerCopyController {

    private final ManagerCopyService service;

    public ManagerCopyController(ManagerCopyService service) {
        this.service = service;
    }

    @GetMapping("/search")
    @Operation(summary = "Search copyCode", description = "Search an individual copy by copyCode/barcode (LIKE match)")
    public List<CopyDTO> search(@RequestParam String q) {
        if (q == null || q.isBlank())
            throw new IllegalArgumentException("Search query is required");
        return service.search(q);
    }

    @GetMapping
    @Operation(summary = "List copies", description = "List copies filtered by variantId, lotId, status")
    public List<CopyDTO> getAll(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) String status) {
        return service.getAll(variantId, lotId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "View copy lifecycle", description = "Full copy lifecycle: lot linkage, location, status, reservation + transaction history")
    public CopyLifecycleDTO getLifecycle(@PathVariable Long id) {
        return service.getLifecycle(id);
    }

    @PutMapping("/{id}/condition")
    @Operation(summary = "Change condition", description = "Change condition for a specific copy after inspection/returns")
    public CopyDTO changeCondition(@PathVariable Long id, @RequestBody ChangeConditionRequest req) {
        return service.changeCondition(id, req);
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Move location / Put-away", description = "Move a copy to a different storage location/bin")
    public CopyDTO moveLocation(@PathVariable Long id, @RequestBody MoveLocationRequest req) {
        return service.moveLocation(id, req);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Mark damaged/lost/found", description = "Mark a copy as DAMAGED, LOST, or AVAILABLE with reasons")
    public CopyDTO markStatus(@PathVariable Long id, @RequestBody MarkStatusRequest req) {
        return service.markStatus(id, req);
    }

    @PutMapping("/{id}/photos")
    @Operation(summary = "Attach copy photos", description = "Attach per-copy photos (JSON array of URLs)")
    public CopyDTO attachPhotos(@PathVariable Long id, @RequestBody AttachPhotosRequest req) {
        return service.attachPhotos(id, req);
    }
}

