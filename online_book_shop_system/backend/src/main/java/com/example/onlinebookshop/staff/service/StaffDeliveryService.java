package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StaffDeliveryService {

    private final StaffOrderRepository staffOrderRepository;

    public StaffDeliveryService(StaffOrderRepository staffOrderRepository) {
        this.staffOrderRepository = staffOrderRepository;
    }

    @Transactional
    public DeliveryOutcomeResult setDeliveryOutcome(long orderId, String outcome, String reason) {
        Order order = staffOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        String status = safeUpper(order.getStatus());
        String oc = safeUpper(outcome);

        if (!status.equals("SHIPPED") && !status.equals("DELIVERED")) {
            throw new RuntimeException("Order phải SHIPPED (hoặc DELIVERED) mới set delivery outcome. status=" + status);
        }

        if (!oc.equals("DELIVERED") && !oc.equals("FAILED")) {
            throw new RuntimeException("Outcome không hợp lệ. Chỉ nhận DELIVERED hoặc FAILED.");
        }

        if (oc.equals("DELIVERED")) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setStatus("DELIVERED");

            if ("PAID".equals(safeUpper(order.getPaymentStatus()))) {
                order.setCompletedAt(LocalDateTime.now());
                order.setStatus("COMPLETED");
                staffOrderRepository.save(order);
                return new DeliveryOutcomeResult("Đã cập nhật DELIVERED và tự động chuyển COMPLETED vì đơn đã thanh toán.");
            }

            staffOrderRepository.save(order);
            return new DeliveryOutcomeResult("Đã cập nhật kết quả giao hàng: DELIVERED.");
        }

        String rs = reason == null ? "" : reason.trim();
        if (rs.isEmpty()) {
            throw new RuntimeException("Khi chọn FAILED, bạn phải nhập lý do thất bại.");
        }

        String oldNote = order.getStaffNote() == null ? "" : order.getStaffNote().trim();
        String append = "[DELIVERY FAILED] " + rs;

        String merged = oldNote.isEmpty()
                ? append
                : oldNote + System.lineSeparator() + append;

        order.setStaffNote(merged);
        staffOrderRepository.save(order);

        return new DeliveryOutcomeResult("Đã ghi nhận giao thất bại vào ghi chú nội bộ.");
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    public record DeliveryOutcomeResult(String message) {
    }
}