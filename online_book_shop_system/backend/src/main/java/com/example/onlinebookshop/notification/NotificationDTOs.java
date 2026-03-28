package com.example.onlinebookshop.notification;

import lombok.*;

public class NotificationDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String body;
        private String type;
        private boolean read;
        private Long refId;
        private String createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UnreadCountDTO {
        private int count;
    }
}
