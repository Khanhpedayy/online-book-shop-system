package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StaffOrderListTest {

    enum OrderStatus { NEW, CONFIRMED, PACKED, SHIPPED, DELIVERED, COMPLETED, CANCELLED }
    enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

    static class Order {
        String orderCode;
        String customerEmail;
        String shipPhone;
        OrderStatus status;
        PaymentStatus paymentStatus;
        LocalDateTime placedAt;

        Order(String code, String email, String phone, OrderStatus st, PaymentStatus ps, LocalDateTime at) {
            this.orderCode = code; this.customerEmail = email; this.shipPhone = phone;
            this.status = st; this.paymentStatus = ps; this.placedAt = at;
        }
    }

    static class OrderQueryService {
        List<Order> filter(List<Order> orders, OrderStatus st, PaymentStatus ps) {
            return orders.stream()
                    .filter(o -> st == null || o.status == st)
                    .filter(o -> ps == null || o.paymentStatus == ps)
                    .collect(Collectors.toList());
        }

        List<Order> search(List<Order> orders, String keyword) {
            if (keyword == null || keyword.isBlank()) return orders;
            String k = keyword.toLowerCase();
            return orders.stream()
                    .filter(o -> o.orderCode.toLowerCase().contains(k)
                            || o.customerEmail.toLowerCase().contains(k)
                            || o.shipPhone.toLowerCase().contains(k))
                    .collect(Collectors.toList());
        }

        List<Order> sortByPlacedAtDesc(List<Order> orders) {
            return orders.stream()
                    .sorted(Comparator.comparing((Order o) -> o.placedAt).reversed())
                    .collect(Collectors.toList());
        }
    }

    private List<Order> seed() {
        return List.of(
                new Order("ODR-0001", "a@gmail.com", "0901", OrderStatus.NEW, PaymentStatus.PENDING, LocalDateTime.now().minusHours(3)),
                new Order("ODR-0002", "b@gmail.com", "0902", OrderStatus.CONFIRMED, PaymentStatus.PAID, LocalDateTime.now().minusHours(1)),
                new Order("ODR-0003", "c@gmail.com", "0903", OrderStatus.NEW, PaymentStatus.PAID, LocalDateTime.now().minusHours(2))
        );
    }

    @Test
    void UTC001_ViewList_Default() {
        var svc = new OrderQueryService();
        var orders = seed();
        assertEquals(3, svc.filter(orders, null, null).size());
    }

    @Test
    void UTC002_Filter_Status_NEW() {
        var svc = new OrderQueryService();
        var result = svc.filter(seed(), OrderStatus.NEW, null);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.status == OrderStatus.NEW));
    }

    @Test
    void UTC003_Filter_Payment_PENDING() {
        var svc = new OrderQueryService();
        var result = svc.filter(seed(), null, PaymentStatus.PENDING);
        assertEquals(1, result.size());
        assertEquals(PaymentStatus.PENDING, result.get(0).paymentStatus);
    }

    @Test
    void UTC004_Search_ByOrderCode() {
        var svc = new OrderQueryService();
        var result = svc.search(seed(), "ODR-0002");
        assertEquals(1, result.size());
        assertEquals("ODR-0002", result.get(0).orderCode);
    }

    @Test
    void UTC005_Sort_ByPlacedAtDesc() {
        var svc = new OrderQueryService();
        var sorted = svc.sortByPlacedAtDesc(seed());
        assertEquals("ODR-0002", sorted.get(0).orderCode); // newest
    }
}