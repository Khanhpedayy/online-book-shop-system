package com.example.onlinebookshop.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbc;

    public NotificationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void create(Long userId, String title, String body, String type, Long refId) {
        jdbc.update(
            "INSERT INTO notifications (user_id, title, body, type, ref_id, is_read, created_at) " +
            "VALUES (?, ?, ?, ?, ?, 0, SYSUTCDATETIME())",
            userId, title, body, type, refId
        );
    }

    public List<NotificationDTOs.NotificationDTO> findByUserId(Long userId) {
        return jdbc.query(
            "SELECT id, title, body, type, is_read, ref_id, created_at " +
            "FROM notifications WHERE user_id=? AND deleted_at IS NULL " +
            "ORDER BY created_at DESC",
            (rs, i) -> {
                NotificationDTOs.NotificationDTO d = new NotificationDTOs.NotificationDTO();
                d.setId(rs.getLong("id"));
                d.setTitle(rs.getString("title"));
                d.setBody(rs.getString("body"));
                d.setType(rs.getString("type"));
                d.setRead(rs.getInt("is_read") == 1);
                d.setRefId(rs.getObject("ref_id") != null ? rs.getLong("ref_id") : null);
                if (rs.getTimestamp("created_at") != null)
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                return d;
            }, userId
        );
    }

    public int countUnread(Long userId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0 AND deleted_at IS NULL",
            Integer.class, userId
        );
        return count != null ? count : 0;
    }

    public void markRead(Long id, Long userId) {
        jdbc.update(
            "UPDATE notifications SET is_read=1 WHERE id=? AND user_id=? AND deleted_at IS NULL",
            id, userId
        );
    }

    public void markAllRead(Long userId) {
        jdbc.update(
            "UPDATE notifications SET is_read=1 WHERE user_id=? AND is_read=0 AND deleted_at IS NULL",
            userId
        );
    }
}
