package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final OrderRepository orderRepository;
    private final EmailOtpService emailService;

    public ShippingServiceImpl(OrderRepository orderRepository, EmailOtpService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getShippingOrders() {
        return orderRepository.findByStatusInAndDeletedAtIsNullOrderByPlacedAtDesc(
                List.of("CONFIRMED", "PACKED", "SHIPPED"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findByDeletedAtIsNullOrderByPlacedAtDesc();
    }

    @Override
    @Transactional
    public Order packOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        validateStatus(order, "CONFIRMED", "Chỉ có thể đóng gói đơn hàng đang ở trạng thái CONFIRMED");

        order.setStatus("PACKED");
        order.setPackedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order shipOrder(Long orderId, String carrier) {
        Order order = findOrderOrThrow(orderId);
        validateStatus(order, "PACKED", "Chỉ có thể giao đơn hàng đã được đóng gói (PACKED)");

        order.setStatus("SHIPPED");
        order.setCarrier(carrier);
        order.setShippedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // System: Send shipping notification email
        try {
            String customerEmail = order.getUser().getEmail();
            emailService.sendShippingNotification(customerEmail, order.getOrderCode(), carrier);
        } catch (Exception e) {
            System.err.println("Không gửi được email thông báo vận chuyển: " + e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public Order deliverOrder(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        validateStatus(order, "SHIPPED", "Chỉ có thể xác nhận giao thành công đơn hàng đang ở trạng thái SHIPPED");

        order.setStatus("DELIVERED");
        order.setDeliveredAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // System: Send delivery confirmation email
        try {
            String customerEmail = order.getUser().getEmail();
            emailService.sendDeliveryConfirmation(customerEmail, order.getOrderCode());
        } catch (Exception e) {
            System.err.println("Không gửi được email xác nhận giao hàng: " + e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public Order failDelivery(Long orderId, String reason) {
        Order order = findOrderOrThrow(orderId);
        validateStatus(order, "SHIPPED", "Chỉ có thể báo giao thất bại cho đơn hàng đang ở trạng thái SHIPPED");

        order.setStatus("DELIVERY_FAILED");
        order.setCancelReason(reason != null ? reason : "Giao hàng thất bại");
        Order saved = orderRepository.save(order);
        return saved;
    }

    // ============ Helpers ============

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));
    }

    private void validateStatus(Order order, String expectedStatus, String errorMessage) {
        if (!expectedStatus.equals(order.getStatus())) {
            throw new IllegalStateException(errorMessage + ". Trạng thái hiện tại: " + order.getStatus());
        }
    }
}
