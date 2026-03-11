package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffOrderDetailTest {

    static class OrderDetail {
        long orderId;
        String shipName;
        String shipPhone;
        String staffNote;

        OrderDetail(long id) { this.orderId = id; }
    }

    static class OrderDetailService {
        OrderDetail getDetail(Long orderId) {
            if (orderId == null || orderId <= 0) throw new IllegalArgumentException("Order not found");
            return new OrderDetail(orderId);
        }

        void updateStaffNote(OrderDetail detail, String note) {
            if (detail == null) throw new IllegalArgumentException("Order not found");
            if (note != null && note.length() > 200) throw new IllegalArgumentException("staff_note too long");
            detail.staffNote = note;
        }
    }

    @Test
    void UTC001_ViewDetail_Success() {
        var svc = new OrderDetailService();
        var detail = svc.getDetail(1L);
        assertEquals(1L, detail.orderId);
    }

    @Test
    void UTC002_ViewDetail_OrderNotFound() {
        var svc = new OrderDetailService();
        assertThrows(IllegalArgumentException.class, () -> svc.getDetail(-1L));
    }

    @Test
    void UTC003_UpdateNote_None() {
        var svc = new OrderDetailService();
        var detail = svc.getDetail(1L);
        svc.updateStaffNote(detail, null);
        assertNull(detail.staffNote);
    }

    @Test
    void UTC004_UpdateNote_Valid() {
        var svc = new OrderDetailService();
        var detail = svc.getDetail(1L);
        svc.updateStaffNote(detail, "ok");
        assertEquals("ok", detail.staffNote);
    }

    @Test
    void UTC005_UpdateNote_Invalid_TooLong() {
        var svc = new OrderDetailService();
        var detail = svc.getDetail(1L);
        var longNote = "a".repeat(201);
        assertThrows(IllegalArgumentException.class, () -> svc.updateStaffNote(detail, longNote));
    }
}