package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffPackingTest {

    enum Status { CONFIRMED, PACKED, NEW, SHIPPED }

    static class Order {
        Status status;
        boolean allPicked;
        Integer boxCount;
    }

    static class PackingService {
        void markPacked(Order order) {
            if (order == null) throw new IllegalArgumentException("Order not found");
            if (order.status != Status.CONFIRMED) throw new IllegalStateException("Invalid status");
            if (!order.allPicked) throw new IllegalStateException("Not fully picked");
            if (order.boxCount != null && order.boxCount <= 0) throw new IllegalArgumentException("Invalid box count");
            order.status = Status.PACKED;
        }
    }

    @Test
    void UTC001_Packed_Success() {
        var order = new Order();
        order.status = Status.CONFIRMED;
        order.allPicked = true;
        order.boxCount = 1;

        var svc = new PackingService();
        svc.markPacked(order);

        assertEquals(Status.PACKED, order.status);
    }

    @Test
    void UTC002_Packed_Fail_OrderNotFound() {
        var svc = new PackingService();
        assertThrows(IllegalArgumentException.class, () -> svc.markPacked(null));
    }

    @Test
    void UTC003_Packed_Fail_InvalidStatus() {
        var order = new Order();
        order.status = Status.NEW;
        order.allPicked = true;

        var svc = new PackingService();
        assertThrows(IllegalStateException.class, () -> svc.markPacked(order));
    }

    @Test
    void UTC004_Packed_Fail_NotPicked() {
        var order = new Order();
        order.status = Status.CONFIRMED;
        order.allPicked = false;

        var svc = new PackingService();
        assertThrows(IllegalStateException.class, () -> svc.markPacked(order));
    }

    @Test
    void UTC005_Packed_Fail_BoxCountInvalid() {
        var order = new Order();
        order.status = Status.CONFIRMED;
        order.allPicked = true;
        order.boxCount = 0;

        var svc = new PackingService();
        assertThrows(IllegalArgumentException.class, () -> svc.markPacked(order));
    }
}