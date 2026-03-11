package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StaffConfirmOrderTest {

    enum Status { NEW, CONFIRMED, CANCELLED, SHIPPED }

    static class Order {
        Long id;
        Status status;
        LocalDateTime confirmedAt;
        Long confirmedBy;
    }

    static class OrderOpsService {
        Order confirm(Order order, Long staffId) {
            if (order == null || order.id == null) throw new IllegalArgumentException("Order not found");
            if (order.status != Status.NEW) throw new IllegalStateException("Cannot confirm in this status");

            order.status = Status.CONFIRMED;
            order.confirmedAt = LocalDateTime.now();
            order.confirmedBy = staffId;
            return order;
        }
    }

    @Test
    void UTC001_Confirm_Success_WhenNEW() {
        var order = new Order();
        order.id = 1L; order.status = Status.NEW;

        var svc = new OrderOpsService();
        var result = svc.confirm(order, 10L);

        assertEquals(Status.CONFIRMED, result.status);
        assertNotNull(result.confirmedAt);
        assertEquals(10L, result.confirmedBy);
    }

    @Test
    void UTC002_Confirm_Fail_OrderNotFound() {
        var svc = new OrderOpsService();
        assertThrows(IllegalArgumentException.class, () -> svc.confirm(null, 10L));
    }

    @Test
    void UTC003_Confirm_Fail_StatusCancelled() {
        var order = new Order();
        order.id = 2L; order.status = Status.CANCELLED;

        var svc = new OrderOpsService();
        assertThrows(IllegalStateException.class, () -> svc.confirm(order, 10L));
    }

    @Test
    void UTC004_Confirm_Fail_StatusShipped() {
        var order = new Order();
        order.id = 3L; order.status = Status.SHIPPED;

        var svc = new OrderOpsService();
        assertThrows(IllegalStateException.class, () -> svc.confirm(order, 10L));
    }
}