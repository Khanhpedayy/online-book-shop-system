package com.example.onlinebookshop.stocktaking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StocktakingService {

    private final StocktakingRepository repo;

    public StocktakingService(StocktakingRepository repo) {
        this.repo = repo;
    }

    public List<StocktakingSessionDTO> getAllSessions() {
        return repo.getAllSessions();
    }

    public StocktakingSessionDTO getSession(String sessionCode) {
        StocktakingSessionDTO session = repo.getSession(sessionCode);
        if (session == null)
            throw new RuntimeException("Stocktaking session not found: " + sessionCode);
        return session;
    }

    @Transactional
    public StocktakingSessionDTO createSession(CreateStocktakingRequest req) {
        String code = "ST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<StocktakingEntryDTO> expected = repo.getExpectedStock(req.getScope());

        if (expected.isEmpty())
            throw new IllegalArgumentException("No stock found for the given scope. Cannot create empty session.");

        StocktakingSessionDTO session = new StocktakingSessionDTO();
        session.setSessionCode(code);
        session.setStatus("OPEN");
        session.setScope(req.getScope());
        session.setNote(req.getNote());
        session.setCreatedAt(LocalDateTime.now().toString());
        session.setEntries(expected);

        repo.saveSession(session);
        return session;
    }

    @Transactional
    public StocktakingSessionDTO recordCount(String sessionCode, RecordCountRequest req) {
        if (req.getVariantId() == null)
            throw new IllegalArgumentException("Variant ID is required");
        if (req.getCountedQty() < 0)
            throw new IllegalArgumentException("Counted quantity must be >= 0");

        StocktakingSessionDTO session = repo.getSession(sessionCode);
        if (session == null)
            throw new RuntimeException("Session not found: " + sessionCode);
        if ("COMPLETED".equals(session.getStatus()))
            throw new IllegalArgumentException("Session already completed. Cannot record counts.");

        // Find matching entry and record count
        boolean found = false;
        for (StocktakingEntryDTO entry : session.getEntries()) {
            if (entry.getVariantId().equals(req.getVariantId())
                    && (req.getLotId() == null || entry.getLotId().equals(req.getLotId()))) {
                entry.setCountedQty(req.getCountedQty());
                entry.setDiff(req.getCountedQty() - entry.getExpectedQty());
                if (req.getNote() != null)
                    entry.setNote(req.getNote());
                found = true;
            }
        }
        if (!found)
            throw new IllegalArgumentException("No matching entry found for variant/lot combination");

        repo.saveSession(session);
        return session;
    }

    @Transactional
    public StocktakingSessionDTO applyAdjustments(String sessionCode, String note) {
        StocktakingSessionDTO session = repo.getSession(sessionCode);
        if (session == null)
            throw new RuntimeException("Session not found: " + sessionCode);
        if ("COMPLETED".equals(session.getStatus()))
            throw new IllegalArgumentException("Session already completed. Cannot apply adjustments again.");

        // Check if any counts were recorded
        boolean hasCounts = session.getEntries().stream()
                .anyMatch(e -> e.getCountedQty() != null);
        if (!hasCounts)
            throw new IllegalArgumentException("No counts recorded yet. Record counts before applying.");

        // Apply diffs as inventory adjustments
        for (StocktakingEntryDTO entry : session.getEntries()) {
            if (entry.getCountedQty() != null && entry.getDiff() != null && entry.getDiff() != 0) {
                repo.updateLotQtyAvailable(entry.getLotId(), entry.getDiff());
                repo.logAdjustment(entry.getVariantId(), entry.getLotId(), entry.getDiff(),
                        "Stocktaking " + sessionCode + ": " + (note != null ? note : ""));
            }
        }

        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now().toString());
        repo.saveSession(session);
        return session;
    }
}

