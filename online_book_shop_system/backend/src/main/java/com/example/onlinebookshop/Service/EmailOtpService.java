package com.example.onlinebookshop.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EmailOtpService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // Lưu trữ OTP tạm thời: Key là email, Value là mã OTP
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public EmailOtpService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String generateAndSendOtp(String email) {
        // Sinh OTP 6 chữ số ngẫu nhiên
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        // Lưu vào cache
        otpCache.put(email, otp);

        // Hủy OTP sau 5 phút
        scheduler.schedule(() -> {
            otpCache.remove(email, otp);
        }, 5, TimeUnit.MINUTES);

        // Gửi email
        sendEmail(email, otp);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String savedOtp = otpCache.get(email);
        if (savedOtp != null && savedOtp.equals(otp)) {
            otpCache.remove(email); // Xóa OTP sau khi sử dụng thành công
            return true;
        }
        return false;
    }

    private void sendEmail(String to, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject("Mã xác thực đăng ký tài khoản - Online Book Shop");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #6c63ff; text-align: center;'>Online Book Shop</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Bạn vừa yêu cầu tạo mới một tài khoản tại hệ thống của chúng tôi. Dưới đây là mã xác thực OTP của bạn:</p>"
                    + "<div style='text-align: center; margin: 20px 0;'>"
                    + "<span style='font-size: 24px; font-weight: bold; background: #f4f4f4; padding: 10px 20px; border-radius: 5px; letter-spacing: 5px;'>"
                    + otp + "</span>"
                    + "</div>"
                    + "<p>Mã này có hiệu lực trong <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='font-size: 12px; color: #888; text-align: center;'>Nếu bạn không yêu cầu đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cấu hình mail sai hoặc lỗi mạng, in OTP ra console để test: " + otp);
        }
    }

    // ============ Password Reset OTP ============

    public String generateAndSendPasswordResetOtp(String email) {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        otpCache.put(email, otp);
        scheduler.schedule(() -> otpCache.remove(email, otp), 5, TimeUnit.MINUTES);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu - Online Book Shop");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #6c63ff; text-align: center;'>Online Book Shop</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Bạn vừa yêu cầu <strong>đặt lại mật khẩu</strong> tại hệ thống của chúng tôi. Dưới đây là mã xác thực OTP:</p>"
                    + "<div style='text-align: center; margin: 20px 0;'>"
                    + "<span style='font-size: 24px; font-weight: bold; background: #f4f4f4; padding: 10px 20px; border-radius: 5px; letter-spacing: 5px;'>"
                    + otp + "</span>"
                    + "</div>"
                    + "<p>Mã này có hiệu lực trong <strong>5 phút</strong>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='font-size: 12px; color: #888; text-align: center;'>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email reset password, OTP: " + otp);
        }

        return otp;
    }

    // ============ Shipping Emails ============

    /**
     * Gửi email thông báo đơn hàng đang được giao
     */
    public void sendShippingNotification(String to, String orderCode, String carrier) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject("Đơn hàng " + orderCode + " đang được giao - Online Book Shop");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #6c63ff; text-align: center;'>📚 Online Book Shop</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Đơn hàng <strong>" + orderCode + "</strong> của bạn đang được giao!</p>"
                    + "<div style='background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0;'>"
                    + "<p style='margin: 5px 0;'>📦 <strong>Mã đơn hàng:</strong> " + orderCode + "</p>"
                    + "<p style='margin: 5px 0;'>🚚 <strong>Nhân viên giao hàng:</strong> " + carrier + "</p>"
                    + "</div>"
                    + "<p>Vui lòng chuẩn bị nhận hàng. Nếu có thắc mắc, hãy liên hệ chúng tôi.</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='font-size: 12px; color: #888; text-align: center;'>Online Book Shop - Cảm ơn bạn đã mua hàng!</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
            System.out.println("Đã gửi email thông báo vận chuyển cho " + to + " (Đơn: " + orderCode + ")");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email thông báo vận chuyển: " + e.getMessage());
        }
    }

    /**
     * Gửi email xác nhận giao hàng thành công
     */
    public void sendDeliveryConfirmation(String to, String orderCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject("Đơn hàng " + orderCode + " đã giao thành công - Online Book Shop");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #2ed573; text-align: center;'>✅ Giao hàng thành công!</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Đơn hàng <strong>" + orderCode + "</strong> của bạn đã được giao thành công!</p>"
                    + "<p>Cảm ơn bạn đã mua sắm tại <strong>Online Book Shop</strong>. Nếu bạn hài lòng với sản phẩm, hãy để lại đánh giá giúp chúng tôi nhé!</p>"
                    + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                    + "<p style='font-size: 12px; color: #888; text-align: center;'>Online Book Shop - Chúc bạn đọc sách vui vẻ! 📖</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
            System.out.println("Đã gửi email xác nhận giao hàng cho " + to + " (Đơn: " + orderCode + ")");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận giao hàng: " + e.getMessage());
        }
    }
}
