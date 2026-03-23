package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Order;

import java.util.List;

public interface ShippingService {

    /** Lấy danh sách đơn hàng cần xử lý giao hàng (CONFIRMED, PACKED, SHIPPED) */
    List<Order> getShippingOrders();

    /** Lấy tất cả đơn hàng (Admin xem tổng quan) */
    List<Order> getAllOrders();

    /** Staff đóng gói đơn hàng */
    Order packOrder(Long orderId);

    /** Staff nhận đơn đi giao (carrier = tên nhân viên) */
    Order shipOrder(Long orderId, String carrier);

    /** Giao hàng thành công */
    Order deliverOrder(Long orderId);

    /** Giao hàng thất bại */
    Order failDelivery(Long orderId, String reason);
}
