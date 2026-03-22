package com.example.onlinebookshop.staff.repo;

import com.example.onlinebookshop.staff.dto.CustomerRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaffCustomerRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StaffCustomerRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CustomerRow> search(String q, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        String sql = """
            SELECT TOP (:limit)
              u.id, u.full_name, u.email, u.phone, u.status
            FROM dbo.users u
            WHERE u.deleted_at IS NULL
              AND (:q IS NULL OR u.full_name LIKE :q OR u.email LIKE :q OR u.phone LIKE :q)
            ORDER BY u.id DESC
            """;

        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("limit", safeLimit)
                .addValue("q", (q == null || q.trim().isEmpty()) ? null : "%" + q.trim() + "%");

        return jdbc.query(sql, ps, (rs, n) -> {
            CustomerRow r = new CustomerRow();
            r.setId(rs.getLong("id"));
            r.setFullName(rs.getString("full_name"));
            r.setEmail(rs.getString("email"));
            r.setPhone(rs.getString("phone"));
            r.setStatus(rs.getString("status"));
            return r;
        });
    }
}