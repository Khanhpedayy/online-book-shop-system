package com.example.onlinebookshop.support;

import lombok.*;
import java.util.List;

public class SupportTicketDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateTicketRequest {
        private String category;   // SHIPPING|PAYMENT|RETURN|PRODUCT|OTHER
        private String subject;
        private String message;
        private Long orderId;      // optional
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TicketSummaryDTO {
        private Long id;
        private String ticketCode;
        private String category;
        private String priority;
        private String status;
        private String subject;
        private String userName;
        private String createdAt;
        private String updatedAt;
        private Integer messageCount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TicketDetailDTO {
        private Long id;
        private String ticketCode;
        private String category;
        private String priority;
        private String status;
        private String subject;
        private Long orderId;
        private String createdAt;
        private List<MessageDTO> messages;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class MessageDTO {
        private String from;
        private String message;
        private String at;
        private boolean isInternal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AdminReplyRequest {
        private String message;
        private String resolution; // RESOLVED | CLOSED (rejected)
    }
}
