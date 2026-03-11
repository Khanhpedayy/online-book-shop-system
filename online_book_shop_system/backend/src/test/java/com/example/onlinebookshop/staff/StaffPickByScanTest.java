package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffPickByScanTest {

    enum CopyStatus { AVAILABLE, RESERVED, PICKED, SOLD, DAMAGED }

    static class OrderItem {
        long id;
        long variantId;
        Long copyId;
        OrderItem(long id, long variantId) { this.id = id; this.variantId = variantId; }
    }

    static class Copy {
        String copyCode;
        long id;
        long variantId;
        CopyStatus status;
        Copy(String code, long id, long variantId, CopyStatus st) {
            this.copyCode = code; this.id = id; this.variantId = variantId; this.status = st;
        }
    }

    static class PickingService {
        void pickByScan(OrderItem item, Copy copy) {
            if (item == null) throw new IllegalArgumentException("Order item not found");
            if (copy == null) throw new IllegalArgumentException("Copy not found");
            if (copy.status != CopyStatus.AVAILABLE) throw new IllegalStateException("Copy not available");
            if (copy.variantId != item.variantId) throw new IllegalArgumentException("Variant mismatch");
            if (item.copyId != null) throw new IllegalStateException("Already picked");

            item.copyId = copy.id;
            copy.status = CopyStatus.PICKED;
        }
    }

    @Test
    void UTC001_Pick_Success() {
        var item = new OrderItem(10, 200);
        var copy = new Copy("CP-001", 1, 200, CopyStatus.AVAILABLE);

        var svc = new PickingService();
        svc.pickByScan(item, copy);

        assertEquals(1L, item.copyId);
        assertEquals(CopyStatus.PICKED, copy.status);
    }

    @Test
    void UTC002_Pick_Fail_OrderItemNotFound() {
        var svc = new PickingService();
        assertThrows(IllegalArgumentException.class, () -> svc.pickByScan(null, new Copy("CP", 1, 200, CopyStatus.AVAILABLE)));
    }

    @Test
    void UTC003_Pick_Fail_CopyNotFound() {
        var svc = new PickingService();
        assertThrows(IllegalArgumentException.class, () -> svc.pickByScan(new OrderItem(10, 200), null));
    }

    @Test
    void UTC004_Pick_Fail_CopyNotAvailable() {
        var item = new OrderItem(10, 200);
        var copy = new Copy("CP-002", 2, 200, CopyStatus.SOLD);

        var svc = new PickingService();
        assertThrows(IllegalStateException.class, () -> svc.pickByScan(item, copy));
    }

    @Test
    void UTC005_Pick_Fail_VariantMismatch() {
        var item = new OrderItem(10, 200);
        var copy = new Copy("CP-003", 3, 999, CopyStatus.AVAILABLE);

        var svc = new PickingService();
        assertThrows(IllegalArgumentException.class, () -> svc.pickByScan(item, copy));
    }

    @Test
    void UTC006_Pick_Fail_DuplicateAssignment() {
        var item = new OrderItem(10, 200);
        item.copyId = 1L;
        var copy = new Copy("CP-004", 4, 200, CopyStatus.AVAILABLE);

        var svc = new PickingService();
        assertThrows(IllegalStateException.class, () -> svc.pickByScan(item, copy));
    }
}