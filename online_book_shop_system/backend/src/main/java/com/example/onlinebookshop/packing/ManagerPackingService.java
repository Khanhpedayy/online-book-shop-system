package com.example.onlinebookshop.packing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerPackingService {
    private final ManagerPackingRepository repo;

    public ManagerPackingService(ManagerPackingRepository repo) {
        this.repo = repo;
    }

    public PackingStatusDTO getStatus(Long orderId) {
        PackingStatusDTO s = repo.getStatus(orderId);
        if (s == null)
            throw new RuntimeException("Order not found: " + orderId);
        return s;
    }

    public PackingSlipDTO getPackingSlip(Long orderId) {
        PackingSlipDTO s = repo.getPackingSlip(orderId);
        if (s == null)
            throw new RuntimeException("Order not found: " + orderId);
        return s;
    }

    @Transactional
    public void markPacked(Long orderId) {
        int u = repo.markPacked(orderId);
        if (u == 0)
            throw new RuntimeException("Order not found: " + orderId);
    }
}

