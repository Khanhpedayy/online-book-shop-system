package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaffAutoAllocationTest {

    enum OrderStatus { CONFIRMED, NEW, CANCELLED }
    enum CopyStatus { AVAILABLE, RESERVED, LOCKED }

    static class OrderItem {
        long variantId;
        long copyId; // 0 = not allocated
        OrderItem(long variantId) { this.variantId = variantId; }
    }

    static class Order {
        long id;
        OrderStatus status;
        List<OrderItem> items = new ArrayList<>();
    }

    static class Copy {
        long id;
        long variantId;
        CopyStatus status;
        int fifoRank; // nhỏ hơn = ưu tiên trước (giả lập FIFO)
        Copy(long id, long variantId, CopyStatus st, int rank) {
            this.id = id; this.variantId = variantId; this.status = st; this.fifoRank = rank;
        }
    }

    static class AllocationService {
        void autoAllocate(Order order, List<Copy> copies) {
            if (order == null) throw new IllegalArgumentException("Order not found");
            if (order.status != OrderStatus.CONFIRMED) throw new IllegalStateException("Invalid status");
            for (var item : order.items) {
                var candidate = copies.stream()
                        .filter(c -> c.variantId == item.variantId)
                        .filter(c -> c.status == CopyStatus.AVAILABLE)
                        .min(Comparator.comparingInt(c -> c.fifoRank))
                        .orElseThrow(() -> new IllegalStateException("Out of stock"));

                candidate.status = CopyStatus.RESERVED;
                item.copyId = candidate.id;
            }
        }
    }

    @Test
    void UTC001_AutoAllocate_Success_FIFO() {
        var order = new Order();
        order.id = 1L; order.status = OrderStatus.CONFIRMED;
        order.items.add(new OrderItem(200L));

        var copies = List.of(
                new Copy(1, 200, CopyStatus.AVAILABLE, 2),
                new Copy(2, 200, CopyStatus.AVAILABLE, 1) // FIFO ưu tiên
        );

        var svc = new AllocationService();
        svc.autoAllocate(order, new ArrayList<>(copies));

        assertEquals(2L, order.items.get(0).copyId);
    }

    @Test
    void UTC002_AutoAllocate_Fail_OrderNotFound() {
        var svc = new AllocationService();
        assertThrows(IllegalArgumentException.class, () -> svc.autoAllocate(null, List.of()));
    }

    @Test
    void UTC003_AutoAllocate_Fail_InvalidStatus() {
        var order = new Order();
        order.id = 2L; order.status = OrderStatus.NEW;
        order.items.add(new OrderItem(200L));

        var svc = new AllocationService();
        assertThrows(IllegalStateException.class, () -> svc.autoAllocate(order, List.of()));
    }

    @Test
    void UTC004_AutoAllocate_Fail_OutOfStock() {
        var order = new Order();
        order.id = 3L; order.status = OrderStatus.CONFIRMED;
        order.items.add(new OrderItem(200L));

        var copies = List.of(new Copy(1, 200, CopyStatus.LOCKED, 1));

        var svc = new AllocationService();
        assertThrows(IllegalStateException.class, () -> svc.autoAllocate(order, new ArrayList<>(copies)));
    }
}