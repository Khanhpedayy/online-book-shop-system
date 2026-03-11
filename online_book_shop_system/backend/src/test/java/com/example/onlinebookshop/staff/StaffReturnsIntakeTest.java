package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffReturnsIntakeTest {

    enum OrderStatus { DELIVERED, COMPLETED, NEW, CANCELLED }
    enum Condition { NEW, LIKE_NEW, GOOD, FAIR }

    static class Order {
        long id;
        OrderStatus status;
    }

    static class ReturnRequest {
        long orderId;
        String reason;
        long orderItemId;
        int quantity;
        String copyCode; // optional
        Condition condition;
    }

    static class ReturnService {
        void createReturn(Order order, ReturnRequest req) {
            if (order == null) throw new IllegalArgumentException("Order not found");
            if (!(order.status == OrderStatus.DELIVERED || order.status == OrderStatus.COMPLETED))
                throw new IllegalStateException("Order not eligible");

            if (req.reason == null || req.reason.isBlank())
                throw new IllegalArgumentException("Reason required");

            if (req.orderItemId <= 0)
                throw new IllegalArgumentException("Invalid order item");

            if (req.quantity <= 0)
                throw new IllegalArgumentException("Invalid quantity");

            if (req.condition == null)
                throw new IllegalArgumentException("Condition required");

            // scan copyCode optional -> nếu có thì validate format tối thiểu
            if (req.copyCode != null && req.copyCode.isBlank())
                throw new IllegalArgumentException("Invalid copy code");
        }
    }

    @Test
    void UTC001_CreateReturn_Success() {
        var order = new Order();
        order.id = 1; order.status = OrderStatus.COMPLETED;

        var req = new ReturnRequest();
        req.orderId = 1;
        req.reason = "Damaged";
        req.orderItemId = 10;
        req.quantity = 1;
        req.copyCode = "CP-001";
        req.condition = Condition.FAIR;

        var svc = new ReturnService();
        assertDoesNotThrow(() -> svc.createReturn(order, req));
    }

    @Test
    void UTC002_CreateReturn_Fail_OrderNotFound() {
        var req = new ReturnRequest();
        req.reason = "x"; req.orderItemId = 1; req.quantity = 1; req.condition = Condition.GOOD;

        var svc = new ReturnService();
        assertThrows(IllegalArgumentException.class, () -> svc.createReturn(null, req));
    }

    @Test
    void UTC003_CreateReturn_Fail_NotEligible() {
        var order = new Order();
        order.id = 1; order.status = OrderStatus.NEW;

        var req = new ReturnRequest();
        req.reason = "x"; req.orderItemId = 1; req.quantity = 1; req.condition = Condition.GOOD;

        var svc = new ReturnService();
        assertThrows(IllegalStateException.class, () -> svc.createReturn(order, req));
    }

    @Test
    void UTC004_CreateReturn_Fail_ReasonInvalid() {
        var order = new Order();
        order.id = 1; order.status = OrderStatus.DELIVERED;

        var req = new ReturnRequest();
        req.reason = " "; req.orderItemId = 1; req.quantity = 1; req.condition = Condition.GOOD;

        var svc = new ReturnService();
        assertThrows(IllegalArgumentException.class, () -> svc.createReturn(order, req));
    }

    @Test
    void UTC008_CreateReturn_Fail_QuantityInvalid() {
        var order = new Order();
        order.id = 1; order.status = OrderStatus.DELIVERED;

        var req = new ReturnRequest();
        req.reason = "ok"; req.orderItemId = 1; req.quantity = 0; req.condition = Condition.GOOD;

        var svc = new ReturnService();
        assertThrows(IllegalArgumentException.class, () -> svc.createReturn(order, req));
    }
}