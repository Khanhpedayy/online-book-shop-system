package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffShipmentTest {

    enum Status { PACKED, SHIPPED, DELIVERED }

    static class Order {
        Status status;
        String carrier;
        String tracking;
    }

    static class ShipmentService {
        void createShipment(Order order, String carrier, String tracking) {
            if (order == null) throw new IllegalArgumentException("Order not found");
            if (carrier == null || carrier.isBlank()) throw new IllegalArgumentException("Carrier required");
            if (tracking == null || tracking.isBlank()) throw new IllegalArgumentException("Tracking required");
            if (tracking.length() > 120) throw new IllegalArgumentException("Tracking too long");
            order.carrier = carrier;
            order.tracking = tracking;
        }

        void markShipped(Order order) {
            if (order.status != Status.PACKED) throw new IllegalStateException("Invalid transition");
            if (order.tracking == null || order.tracking.isBlank()) throw new IllegalArgumentException("Tracking required");
            order.status = Status.SHIPPED;
        }

        void markDelivered(Order order) {
            if (order.status != Status.SHIPPED) throw new IllegalStateException("Invalid transition");
            order.status = Status.DELIVERED;
        }
    }

    @Test
    void UTC001_CreateShipment_Success() {
        var order = new Order();
        order.status = Status.PACKED;

        var svc = new ShipmentService();
        svc.createShipment(order, "GHN", "TRK123");

        assertEquals("GHN", order.carrier);
        assertEquals("TRK123", order.tracking);
    }

    @Test
    void UTC003_MarkShipped_Success() {
        var order = new Order();
        order.status = Status.PACKED;
        order.tracking = "TRK123";

        var svc = new ShipmentService();
        svc.markShipped(order);

        assertEquals(Status.SHIPPED, order.status);
    }

    @Test
    void UTC004_MarkShipped_Fail_InvalidTransition() {
        var order = new Order();
        order.status = Status.SHIPPED;

        var svc = new ShipmentService();
        assertThrows(IllegalStateException.class, () -> svc.markShipped(order));
    }

    @Test
    void UTC005_MarkDelivered_Success() {
        var order = new Order();
        order.status = Status.SHIPPED;

        var svc = new ShipmentService();
        svc.markDelivered(order);

        assertEquals(Status.DELIVERED, order.status);
    }

    @Test
    void UTC006_CreateShipment_Fail_MissingTracking() {
        var order = new Order();
        order.status = Status.PACKED;

        var svc = new ShipmentService();
        assertThrows(IllegalArgumentException.class, () -> svc.createShipment(order, "GHN", " "));
    }
}