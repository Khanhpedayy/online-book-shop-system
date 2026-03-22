package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import com.example.onlinebookshop.staff.dto.StaffAlert;
import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import com.example.onlinebookshop.staff.repo.StaffAlertRepository;
import com.example.onlinebookshop.staff.repo.StaffOrderQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaffOrderService {

    private final StaffOrderRepository staffOrderRepository;
    private final StaffOrderQueryRepository queryRepository;
    private final StaffAlertRepository alertRepository;

    public StaffOrderService(StaffOrderRepository staffOrderRepository,
                             StaffOrderQueryRepository queryRepository,
                             StaffAlertRepository alertRepository) {
        this.staffOrderRepository = staffOrderRepository;
        this.queryRepository = queryRepository;
        this.alertRepository = alertRepository;
    }

    public List<OrderListRow> getAll(OrderFilter filter) {
        return queryRepository.findOrders(filter, 200);
    }

    public Order getById(Long id) {
        return staffOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<StaffAlert> getAlerts() {
        return alertRepository.getAlerts(20);
    }

    public List<Order> getTodoList() {
        return staffOrderRepository.findTodoTop10();
    }

    public OrderDetailView getDetail(long orderId) {
        return queryRepository.getOrderDetail(orderId);
    }

    @Transactional
    public void updateStaffNote(Long orderId, String staffNote) {
        Order order = getById(orderId);
        order.setStaffNote(staffNote == null ? null : staffNote.trim());
        staffOrderRepository.save(order);
    }

    @Transactional
    public void updateShipment(Long orderId, String carrier, String trackingCode) {
        Order order = getById(orderId);
        order.setCarrier(carrier == null ? null : carrier.trim());
        order.setTrackingCode(trackingCode == null ? null : trackingCode.trim());
        staffOrderRepository.save(order);
    }

    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = getById(orderId);
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new RuntimeException("paymentStatus is required");
        }
        order.setPaymentStatus(paymentStatus.trim().toUpperCase());
        staffOrderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long id, String newStatus) {
        Order order = getById(id);

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new RuntimeException("New status is required");
        }

        String ns = newStatus.trim().toUpperCase();
        String current = order.getStatus() == null ? "" : order.getStatus().toUpperCase();

        if (current.equals("COMPLETED") || current.equals("CANCELLED")) {
            throw new RuntimeException("Cannot modify final state order");
        }

        switch (ns) {
            case "NEW" -> {
                // giữ nguyên, không set timestamp
            }
            case "CONFIRMED" -> order.setConfirmedAt(LocalDateTime.now());
            case "PACKED" -> order.setPackedAt(LocalDateTime.now());
            case "SHIPPED" -> order.setShippedAt(LocalDateTime.now());
            case "DELIVERED" -> order.setDeliveredAt(LocalDateTime.now());
            case "COMPLETED" -> order.setCompletedAt(LocalDateTime.now());
            case "CANCELLED" -> order.setCancelledAt(LocalDateTime.now());
            default -> throw new RuntimeException("Unsupported status: " + ns);
        }

        order.setStatus(ns);
        staffOrderRepository.save(order);
    }

    public StaffDashboardStats getDashboardStats() {
        try {
            long newOrders = staffOrderRepository.countNewOrders();
            long pendingPayments = staffOrderRepository.countPendingPayments();
            long toPack = staffOrderRepository.countToPack();

            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            long shippedToday = staffOrderRepository.countShippedBetween(start, end);

            LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(24);
            long overdue = staffOrderRepository.countOverdueBefore(overdueThreshold);

            return new StaffDashboardStats(newOrders, pendingPayments, toPack, shippedToday, overdue);
        } catch (Exception ex) {
            return new StaffDashboardStats(0, 0, 0, 0, 0);
        }
    }
}