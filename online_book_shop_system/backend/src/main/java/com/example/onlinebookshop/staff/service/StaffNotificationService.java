package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.notification.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class StaffNotificationService {

    private final NotificationService notificationService;

    public StaffNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyOrderShipped(long userId, String orderCode, long orderId) {
        String title = "Đơn hàng đang được giao";
        String body = "Đơn hàng " + orderCode + " của bạn đang trên đường giao đến bạn!";
        notificationService.send(userId, title, body, "ORDER_SHIPPED", orderId);
    }

    public void notifyOrderCompleted(long userId, String orderCode, long orderId) {
        String title = "Giao hàng thành công";
        String body = "Đơn hàng " + orderCode + " đã được giao thành công. Cảm ơn bạn đã mua hàng!";
        notificationService.send(userId, title, body, "ORDER_COMPLETED", orderId);
    }

    public void notifyOrderCancelled(long userId, String orderCode, long orderId, String reason) {
        String title = "Giao hàng thất bại";
        String body = "Đơn hàng " + orderCode + " giao không thành công.";
        if (reason != null && !reason.trim().isEmpty()) {
            body += " Lý do: " + reason;
        }
        notificationService.send(userId, title, body, "ORDER_CANCELLED", orderId);
    }
}
