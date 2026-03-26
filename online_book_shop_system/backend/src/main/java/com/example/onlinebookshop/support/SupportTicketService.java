package com.example.onlinebookshop.support;

import com.example.onlinebookshop.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository repo;
    private final NotificationService notifService;

    public SupportTicketService(SupportTicketRepository repo, NotificationService notifService) {
        this.repo = repo;
        this.notifService = notifService;
    }

    /** Customer tạo ticket */
    public void createTicket(Long userId, SupportTicketDTOs.CreateTicketRequest req) {
        if (req.getSubject() == null || req.getSubject().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập tiêu đề yêu cầu");
        if (req.getMessage() == null || req.getMessage().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập nội dung yêu cầu");
        if (req.getCategory() == null || req.getCategory().isBlank())
            throw new IllegalArgumentException("Vui lòng chọn loại vấn đề");
        repo.create(userId, req);
    }

    /** Customer xem ticket của mình */
    public List<SupportTicketDTOs.TicketSummaryDTO> getMyTickets(Long userId) {
        return repo.findByUser(userId);
    }

    /** Customer xem chi tiết */
    public SupportTicketDTOs.TicketDetailDTO getTicketDetail(Long ticketId, Long userId) {
        SupportTicketDTOs.TicketDetailDTO detail = repo.findById(ticketId);
        if (detail == null) throw new IllegalStateException("Ticket không tồn tại");
        return detail;
    }

    /** Admin xem tất cả */
    public List<SupportTicketDTOs.TicketSummaryDTO> getAllTickets(String status) {
        return repo.findAll(status);
    }

    /** Admin xem chi tiết */
    public SupportTicketDTOs.TicketDetailDTO getAdminTicketDetail(Long ticketId) {
        SupportTicketDTOs.TicketDetailDTO detail = repo.findById(ticketId);
        if (detail == null) throw new IllegalStateException("Ticket không tồn tại");
        return detail;
    }

    /** Admin reply và resolve */
    public void adminReply(Long ticketId, String adminName, SupportTicketDTOs.AdminReplyRequest req) {
        if (req.getMessage() == null || req.getMessage().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập nội dung phản hồi");
        String resolution = req.getResolution();
        if (resolution == null || resolution.isBlank()) resolution = "IN_PROGRESS";

        repo.adminReply(ticketId, adminName, req.getMessage(), resolution);

        Long customerId = repo.getTicketUserId(ticketId);
        if (customerId == null) return;

        // Gửi notification tới customer
        if ("RESOLVED".equals(resolution)) {
            notifService.send(
                customerId,
                "Yêu cầu hỗ trợ đã được giải quyết",
                "Yêu cầu của bạn đã được xử lý. Phản hồi: " + req.getMessage(),
                "SUPPORT_RESOLVED",
                ticketId
            );
        } else if ("CLOSED".equals(resolution)) {
            notifService.send(
                customerId,
                "Yêu cầu hỗ trợ đã bị từ chối",
                "Yêu cầu của bạn không được chấp nhận. Lý do: " + req.getMessage(),
                "SUPPORT_CLOSED",
                ticketId
            );
        } else {
            notifService.send(
                customerId,
                "Phản hồi từ bộ phận hỗ trợ",
                req.getMessage(),
                "SUPPORT_REPLY",
                ticketId
            );
        }
    }
}
