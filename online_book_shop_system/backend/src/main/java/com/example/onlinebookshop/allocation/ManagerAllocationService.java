package com.example.onlinebookshop.allocation;

import org.springframework.stereotype.Service;

@Service
public class ManagerAllocationService {

    private final ManagerAllocationRepository repo;

    public ManagerAllocationService(ManagerAllocationRepository repo) {
        this.repo = repo;
    }

    public AllocationSettingsDTO getSettings() {
        return repo.getSettings();
    }

    public AllocationSettingsDTO updateSettings(AllocationSettingsDTO dto) {
        if (dto.getFifoBy() == null || dto.getFifoBy().isBlank())
            throw new IllegalArgumentException("FIFO strategy is required");
        if (!dto.getFifoBy().equals("LOT") && !dto.getFifoBy().equals("COPY"))
            throw new IllegalArgumentException("FIFO must be LOT or COPY");
        if (dto.getReservationTtlMin() <= 0)
            throw new IllegalArgumentException("Reservation TTL must be > 0 minutes");
        if (dto.getConditionPriority() != null
                && !dto.getConditionPriority().equals("NEWEST_FIRST")
                && !dto.getConditionPriority().equals("CHEAPEST_FIRST"))
            throw new IllegalArgumentException("Condition priority must be NEWEST_FIRST or CHEAPEST_FIRST");
        repo.saveSettings(dto);
        return repo.getSettings();
    }
}

