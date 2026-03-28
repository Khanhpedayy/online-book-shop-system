package com.example.onlinebookshop.notification;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void send(Long userId, String title, String body, String type, Long refId) {
        repo.create(userId, title, body, type, refId);
    }

    public List<NotificationDTOs.NotificationDTO> getAll(Long userId) {
        return repo.findByUserId(userId);
    }

    public NotificationDTOs.UnreadCountDTO getUnreadCount(Long userId) {
        return new NotificationDTOs.UnreadCountDTO(repo.countUnread(userId));
    }

    public void markRead(Long id, Long userId) {
        repo.markRead(id, userId);
    }

    public void markAllRead(Long userId) {
        repo.markAllRead(userId);
    }
}
