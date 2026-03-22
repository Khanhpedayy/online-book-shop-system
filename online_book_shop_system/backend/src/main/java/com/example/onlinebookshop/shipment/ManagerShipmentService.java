package com.example.onlinebookshop.shipment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerShipmentService {
    private final ManagerShipmentRepository repo;

    public ManagerShipmentService(ManagerShipmentRepository repo) {
        this.repo = repo;
    }

    public ShipmentDTO getByOrderId(Long orderId) {
        return repo.findByOrderId(orderId);
    }

    @Transactional
    public void create(Long orderId, CreateShipmentRequest req) {
        repo.create(orderId, req);
    }

    @Transactional
    public void markShipped(Long orderId) {
        int u = repo.markShipped(orderId);
        if (u == 0)
            throw new RuntimeException("No shipment to mark as shipped for order: " + orderId);
    }

    @Transactional
    public void updateDelivery(Long orderId, UpdateDeliveryRequest req) {
        int u = repo.updateDelivery(orderId, req.getOutcome());
        if (u == 0)
            throw new RuntimeException("No shipped shipment found for order: " + orderId);
    }
}

