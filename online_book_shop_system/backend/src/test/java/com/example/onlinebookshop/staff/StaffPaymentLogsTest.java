package com.example.onlinebookshop.staff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffPaymentLogsTest {

    enum OrderPay { PENDING, PAID }
    enum PayStatus { CREATED, SUCCEEDED, FAILED }

    static class Order {
        long id;
        OrderPay paymentStatus;
    }

    static class Payment {
        long orderId;
        PayStatus status;
        Payment(long orderId, PayStatus st) { this.orderId = orderId; this.status = st; }
    }

    static class PaymentService {
        Payment viewPayment(Order order, Payment payment) {
            if (order == null) throw new IllegalArgumentException("Order not found");
            return payment; // may be null => no payment info
        }

        boolean isMismatch(Order order, Payment payment) {
            if (payment == null) return false;
            if (order.paymentStatus == OrderPay.PAID && payment.status != PayStatus.SUCCEEDED) return true;
            if (order.paymentStatus == OrderPay.PENDING && payment.status == PayStatus.SUCCEEDED) return true;
            return false;
        }

        PayStatus recheckProviderMock(PayStatus providerReturn) {
            return providerReturn; // giả lập provider
        }
    }

    @Test
    void UTC001_ViewLog_Success() {
        var order = new Order();
        order.id = 1; order.paymentStatus = OrderPay.PENDING;
        var payment = new Payment(1, PayStatus.CREATED);

        var svc = new PaymentService();
        var result = svc.viewPayment(order, payment);

        assertNotNull(result);
    }

    @Test
    void UTC003_ViewLog_NoPaymentInfo() {
        var order = new Order();
        order.id = 1; order.paymentStatus = OrderPay.PENDING;

        var svc = new PaymentService();
        var result = svc.viewPayment(order, null);

        assertNull(result);
    }

    @Test
    void UTC004_Mismatch_Detected() {
        var order = new Order();
        order.id = 1; order.paymentStatus = OrderPay.PENDING;
        var payment = new Payment(1, PayStatus.SUCCEEDED);

        var svc = new PaymentService();
        assertTrue(svc.isMismatch(order, payment));
    }


}