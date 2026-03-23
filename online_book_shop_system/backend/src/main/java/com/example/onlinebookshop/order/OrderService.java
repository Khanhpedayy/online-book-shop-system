package com.example.onlinebookshop.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("orderMgmtService")
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public List<OrderListDTO> getAll(String status, String paymentStatus, String deliveryStatus,
                                     String search, String sortBy, String sortDir) {
        return repo.findAll(status, paymentStatus, deliveryStatus, search, sortBy, sortDir);
    }

    public OrderDetailDTO getById(Long id) {
        OrderDetailDTO detail = repo.findById(id);
        if (detail == null)
            throw new RuntimeException("Order not found: " + id);
        return detail;
    }

    @Transactional
    public void addNote(Long orderId, AddNoteRequest req) {
        repo.addNote(orderId, req.getStaffId(), req.getContent());
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        int updated = repo.confirmOrder(orderId);
        if (updated == 0)
            throw new RuntimeException("Order not found or already confirmed: " + orderId);
    }
}

