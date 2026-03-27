package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ShippingServiceImpl implements ShippingService {

    private final OrderRepository orderRepository;

    public ShippingServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getShippingOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getDeletedAt() == null)
                .filter(o -> List.of("CONFIRMED", "PACKED", "SHIPPED").contains(o.getStatus()))
                .toList();
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getDeletedAt() == null)
                .toList();
    }

    @Override
    public Order packOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể đóng gói đơn hàng ở trạng thái CONFIRMED. Trạng thái hiện tại: " + order.getStatus());
        }

        order.setStatus("PACKED");
        order.setPackedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order shipOrder(Long orderId, String carrier) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"PACKED".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể giao đơn hàng ở trạng thái PACKED. Trạng thái hiện tại: " + order.getStatus());
        }

        order.setStatus("SHIPPED");
        order.setCarrier(carrier);
        order.setShippedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xác nhận giao cho đơn ở trạng thái SHIPPED. Trạng thái hiện tại: " + order.getStatus());
        }

        order.setStatus("DELIVERED");
        order.setDeliveredAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public Order failDelivery(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể báo thất bại cho đơn ở trạng thái SHIPPED. Trạng thái hiện tại: " + order.getStatus());
        }

        order.setStatus("DELIVERY_FAILED");
        order.setStaffNote(reason);
        return orderRepository.save(order);
    }
}
