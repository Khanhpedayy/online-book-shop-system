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
            // Log out the error if it happens but we don't necessarily crash because we
            // fallback to printing OTP when testing locally
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Cấu hình mail sai hoặc lỗi mạng, in OTP ra console để test: " + otp);
        }
    }
}
