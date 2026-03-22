package com.example.onlinebookshop.picking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/orders/{orderId}")
@CrossOrigin(origins = "*")
@Tag(name = "13. Picking & Allocation")
public class ManagerPickingController {

    private final ManagerPickingService service;

    public ManagerPickingController(ManagerPickingService service) {
        this.service = service;
    }

    @PostMapping("/allocate")
    @Operation(summary = "Auto allocate (FIFO)", description = "Automatically allocate inventory using FIFO rules by lot/copy")
    public ResponseEntity<Void> autoAllocate(@PathVariable Long orderId) {
        service.autoAllocate(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pick-items")
    @Operation(summary = "View pick list", description = "View allocated lots/copies for order")
    public PickListDTO getPickList(@PathVariable Long orderId) {
        return service.getPickList(orderId);
    }

    @PostMapping("/pick")
    @Operation(summary = "Manual pick", description = "Scan copyCode to manually assign the exact copy")
    public ResponseEntity<Void> manualPick(@PathVariable Long orderId, @RequestBody ManualPickRequest req) {
        try {
            service.manualPick(orderId, req);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/pick/{itemId}/confirm")
    @Operation(summary = "Confirm picked", description = "Mark a pick item as confirmed (scan confirm)")
    public ResponseEntity<Void> confirmPick(@PathVariable Long orderId, @PathVariable Long itemId,
                                            @RequestParam Long staffId) {
        try {
            service.confirmPick(itemId, staffId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/pick/{itemId}/unpick")
    @Operation(summary = "Unpick/Reassign", description = "Un-assign a wrongly picked copy and reassign")
    public ResponseEntity<Void> unpick(@PathVariable Long orderId, @PathVariable Long itemId) {
        service.unpick(itemId);
        return ResponseEntity.ok().build();
    }
}

