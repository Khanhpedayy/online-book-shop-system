package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

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

    // ─── Wallet notifications ───────────────────────────────────────

    public void notifyWalletCredited(long userId, BigDecimal amount, String orderCode) {
        String amountStr = formatVnd(amount);
        String title = "Tiền hoàn về ví ảo";
        String body = "Đơn hàng " + orderCode + " giao thất bại. Chúng tôi đã hoàn " + amountStr
                + " vào ví ảo của bạn (đã trừ 10.000đ phí vận chuyển). Bạn có thể yêu cầu rút tiền trong trang Profile.";
        notificationService.send(userId, title, body, "WALLET_CREDITED", null);
    }

    public void notifyWithdrawalApproved(long userId, BigDecimal amount) {
        String amountStr = formatVnd(amount);
        String title = "Yêu cầu rút tiền được duyệt";
        String body = "Yêu cầu rút " + amountStr + " của bạn đã được duyệt. Tiền sẽ được chuyển về tài khoản ngân hàng bạn đã cung cấp trong vài ngày làm việc.";
        notificationService.send(userId, title, body, "WITHDRAWAL_APPROVED", null);
    }

    public void notifyWithdrawalRejected(long userId, BigDecimal amount, String reason) {
        String amountStr = formatVnd(amount);
        String title = "Yêu cầu rút tiền bị từ chối";
        String body = "Yêu cầu rút " + amountStr + " của bạn đã bị từ chối. "
                + (reason != null && !reason.isBlank() ? "Lý do: " + reason.trim() + ". " : "")
                + "Số tiền đã được hoàn lại vào ví ảo của bạn.";
        notificationService.send(userId, title, body, "WITHDRAWAL_REJECTED", null);
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0₫";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(amount) + "₫";
    }
}

