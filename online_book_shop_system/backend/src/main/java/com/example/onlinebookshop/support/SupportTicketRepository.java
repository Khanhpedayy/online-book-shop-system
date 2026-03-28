package com.example.onlinebookshop.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class SupportTicketRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public SupportTicketRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.mapper = new ObjectMapper();
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Tạo ticket mới */
    public void create(Long userId, SupportTicketDTOs.CreateTicketRequest req) {
        String code = "TK-" + System.currentTimeMillis();
        String firstMsg = buildMessagesJson("customer", req.getMessage());
        jdbc.update(
            "INSERT INTO support_tickets (ticket_code, user_id, order_id, category, priority, status, subject, messages_json, created_at) " +
            "VALUES (?, ?, ?, ?, 'NORMAL', 'OPEN', ?, ?, SYSUTCDATETIME())",
            code, userId, req.getOrderId(), req.getCategory(), req.getSubject(), firstMsg
        );
    }

    /** Danh sách ticket của user */
    public List<SupportTicketDTOs.TicketSummaryDTO> findByUser(Long userId) {
        return jdbc.query(
            "SELECT id, ticket_code, category, priority, status, subject, created_at, updated_at, " +
            "LEN(messages_json) AS msg_len " +
            "FROM support_tickets WHERE user_id=? AND deleted_at IS NULL ORDER BY created_at DESC",
            (rs, i) -> {
                SupportTicketDTOs.TicketSummaryDTO d = new SupportTicketDTOs.TicketSummaryDTO();
                d.setId(rs.getLong("id"));
                d.setTicketCode(rs.getString("ticket_code"));
                d.setCategory(rs.getString("category"));
                d.setPriority(rs.getString("priority"));
                d.setStatus(rs.getString("status"));
                d.setSubject(rs.getString("subject"));
                if (rs.getTimestamp("created_at") != null)
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                if (rs.getTimestamp("updated_at") != null)
                    d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
                return d;
            }, userId
        );
    }

    /** Admin: tất cả ticket */
    public List<SupportTicketDTOs.TicketSummaryDTO> findAll(String statusFilter) {
        String whereStatus = (statusFilter != null && !statusFilter.isBlank())
            ? "AND t.status='" + statusFilter.replace("'", "") + "'"
            : "";
        String sql = "SELECT t.id, t.ticket_code, t.category, t.priority, t.status, t.subject, " +
                     "t.created_at, t.updated_at, u.full_name AS user_name " +
                     "FROM support_tickets t JOIN users u ON t.user_id = u.id " +
                     "WHERE t.deleted_at IS NULL " + whereStatus + " ORDER BY t.created_at DESC";
        return jdbc.query(sql, (rs, i) -> {
            SupportTicketDTOs.TicketSummaryDTO d = new SupportTicketDTOs.TicketSummaryDTO();
            d.setId(rs.getLong("id"));
            d.setTicketCode(rs.getString("ticket_code"));
            d.setCategory(rs.getString("category"));
            d.setPriority(rs.getString("priority"));
            d.setStatus(rs.getString("status"));
            d.setSubject(rs.getString("subject"));
            d.setUserName(rs.getString("user_name"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            if (rs.getTimestamp("updated_at") != null)
                d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
            return d;
        });
    }

    /** Chi tiết 1 ticket */
    public SupportTicketDTOs.TicketDetailDTO findById(Long id) {
        return jdbc.query(
            "SELECT id, ticket_code, category, priority, status, subject, order_id, created_at, messages_json " +
            "FROM support_tickets WHERE id=? AND deleted_at IS NULL",
            (rs, i) -> {
                SupportTicketDTOs.TicketDetailDTO d = new SupportTicketDTOs.TicketDetailDTO();
                d.setId(rs.getLong("id"));
                d.setTicketCode(rs.getString("ticket_code"));
                d.setCategory(rs.getString("category"));
                d.setPriority(rs.getString("priority"));
                d.setStatus(rs.getString("status"));
                d.setSubject(rs.getString("subject"));
                d.setOrderId(rs.getObject("order_id") != null ? rs.getLong("order_id") : null);
                if (rs.getTimestamp("created_at") != null)
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                d.setMessages(parseMessages(rs.getString("messages_json")));
                return d;
            }, id
        ).stream().findFirst().orElse(null);
    }

    /** Lấy user_id của ticket (để gửi notification) */
    public Long getTicketUserId(Long ticketId) {
        List<Long> rows = jdbc.query(
            "SELECT user_id FROM support_tickets WHERE id=?",
            (rs, i) -> rs.getLong("user_id"), ticketId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Admin reply + đổi status */
    public void adminReply(Long ticketId, String adminName, String message, String newStatus) {
        // Lấy messages_json hiện tại
        String existing = jdbc.queryForObject(
            "SELECT messages_json FROM support_tickets WHERE id=?", String.class, ticketId
        );
        List<Map<String, Object>> msgs = parseMessagesRaw(existing);
        Map<String, Object> newMsg = new LinkedHashMap<>();
        newMsg.put("from", "admin");
        newMsg.put("senderName", adminName);
        newMsg.put("message", message);
        newMsg.put("at", LocalDateTime.now().format(FMT));
        newMsg.put("isInternal", false);
        msgs.add(newMsg);
        String json = toJson(msgs);
        jdbc.update(
            "UPDATE support_tickets SET status=?, messages_json=?, updated_at=SYSUTCDATETIME() WHERE id=?",
            newStatus, json, ticketId
        );
    }

    // helpers
    private String buildMessagesJson(String from, String message) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("from", from);
        msg.put("message", message);
        msg.put("at", LocalDateTime.now().format(FMT));
        msg.put("isInternal", false);
        return toJson(List.of(msg));
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); } catch(Exception e) { return "[]"; }
    }

    @SuppressWarnings("unchecked")
    private List<SupportTicketDTOs.MessageDTO> parseMessages(String json) {
        try {
            List<Map<String, Object>> raw = mapper.readValue(json, new TypeReference<>() {});
            List<SupportTicketDTOs.MessageDTO> result = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                SupportTicketDTOs.MessageDTO dto = new SupportTicketDTOs.MessageDTO();
                dto.setFrom(String.valueOf(m.getOrDefault("from", "")));
                dto.setMessage(String.valueOf(m.getOrDefault("message", "")));
                dto.setAt(String.valueOf(m.getOrDefault("at", "")));
                dto.setInternal(Boolean.TRUE.equals(m.get("isInternal")));
                result.add(dto);
            }
            return result;
        } catch(Exception e) { return new ArrayList<>(); }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseMessagesRaw(String json) {
        try { return mapper.readValue(json, new TypeReference<>() {}); }
        catch(Exception e) { return new ArrayList<>(); }
    }
}
