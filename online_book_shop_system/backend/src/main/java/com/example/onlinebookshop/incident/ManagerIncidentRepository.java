package com.example.onlinebookshop.incident;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ManagerIncidentRepository {
    private final JdbcTemplate jdbc;

    public ManagerIncidentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<IncidentDTO> findAll() {
        return jdbc.query(
                "SELECT i.id, i.type, i.order_id, o.order_code, i.copy_id, c.copy_code, "
                        + "u.full_name AS reported_by_name, i.description, i.status, i.created_at "
                        + "FROM incidents i "
                        + "LEFT JOIN orders o ON i.order_id=o.id "
                        + "LEFT JOIN copies c ON i.copy_id=c.id "
                        + "LEFT JOIN users u ON i.reported_by=u.id "
                        + "ORDER BY i.created_at DESC",
                (rs, i) -> {
                    IncidentDTO d = new IncidentDTO();
                    d.setId(rs.getLong("id"));
                    d.setType(rs.getString("type"));
                    d.setOrderId(rs.getObject("order_id") != null ? rs.getLong("order_id") : null);
                    d.setOrderCode(rs.getString("order_code"));
                    d.setCopyId(rs.getObject("copy_id") != null ? rs.getLong("copy_id") : null);
                    d.setCopyCode(rs.getString("copy_code"));
                    d.setReportedByName(rs.getString("reported_by_name"));
                    d.setDescription(rs.getString("description"));
                    d.setStatus(rs.getString("status"));
                    if (rs.getTimestamp("created_at") != null)
                        d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
                    return d;
                });
    }

    public void create(CreateIncidentRequest req) {
        jdbc.update("INSERT INTO incidents (type, order_id, copy_id, reported_by, description) VALUES (?,?,?,?,?)",
                req.getType(), req.getOrderId(), req.getCopyId(), req.getReportedBy(), req.getDescription());
    }
}

