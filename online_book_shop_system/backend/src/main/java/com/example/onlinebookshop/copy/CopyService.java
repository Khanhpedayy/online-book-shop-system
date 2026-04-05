package com.example.onlinebookshop.copy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CopyService {

    private final CopyRepository repo;

    public CopyService(CopyRepository repo) {
        this.repo = repo;
    }

    public List<CopyDTO> search(String query) {
        return repo.search(query);
    }

    public List<CopyDTO> getAll(Long variantId, Long lotId, String status) {
        return repo.findAll(variantId, lotId, status);
    }

    public CopyLifecycleDTO getLifecycle(Long id) {
        CopyLifecycleDTO lc = repo.findLifecycleById(id);
        if (lc == null)
            throw new RuntimeException("Copy not found: " + id);
        return lc;
    }


    @Transactional
    public CopyDTO moveLocation(Long id, MoveLocationRequest req) {
        if (req.getNewLocation() == null || req.getNewLocation().isBlank())
            throw new IllegalArgumentException("New location is required");

        CopyDTO c = repo.findById(id);
        if (c == null)
            throw new RuntimeException("Copy not found: " + id);
        String oldLocation = c.getLocation();
        repo.updateLocation(id, req.getNewLocation());
        repo.logTransaction("TRANSFER", c.getVariantId(), c.getLotId(), id, 1,
                oldLocation, req.getNewLocation(), "ADJUSTMENT", null, "TRANSFER", req.getNote());
        return repo.findById(id);
    }

    @Transactional
    public CopyDTO markStatus(Long id, MarkStatusRequest req) {
        if (req.getStatus() == null || req.getStatus().isBlank())
            throw new IllegalArgumentException("Status is required");

        CopyDTO c = repo.findById(id);
        if (c == null)
            throw new RuntimeException("Copy not found: " + id);
        String validStatus = req.getStatus();
        if (!validStatus.equals("DAMAGED") && !validStatus.equals("LOST") && !validStatus.equals("AVAILABLE")) {
            throw new IllegalArgumentException(
                    "Invalid status: " + validStatus + ". Must be DAMAGED, LOST, or AVAILABLE.");
        }
        repo.updateStatus(id, validStatus);

        // Update lot qty
        String reason = validStatus.equals("AVAILABLE") ? "FOUND" : validStatus;
        repo.logTransaction("ADJUST", c.getVariantId(), c.getLotId(), id, 1,
                null, null, "ADJUSTMENT", null, reason, req.getNote());
        return repo.findById(id);
    }

    @Transactional
    public CopyDTO attachPhotos(Long id, AttachPhotosRequest req) {
        if (req.getImagesJson() == null || req.getImagesJson().isBlank())
            throw new IllegalArgumentException("Images JSON is required");

        CopyDTO c = repo.findById(id);
        if (c == null)
            throw new RuntimeException("Copy not found: " + id);
        repo.updatePhotos(id, req.getImagesJson());
        return repo.findById(id);
    }
}

