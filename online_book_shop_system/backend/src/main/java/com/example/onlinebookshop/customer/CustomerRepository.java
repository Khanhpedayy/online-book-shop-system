package com.example.onlinebookshop.customer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbc;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CustomerDTO> search(String query) {
        String sql = "SELECT u.id, u.full_name, u.email, u.phone, u.status, u.created_at, "
                + "(SELECT COUNT(*) FROM orders o WHERE o.user_id=u.id) AS order_count "
                + "FROM users u WHERE u.role_id=4 AND "
                + "(u.full_name LIKE ? OR u.email LIKE ? OR u.phone LIKE ?) "
                + "ORDER BY u.full_name";
        String pat = "%" + query + "%";
        return jdbc.query(sql, (rs, i) -> {
            CustomerDTO d = new CustomerDTO();
            d.setId(rs.getLong("id"));
            d.setFullName(rs.getString("full_name"));
            d.setEmail(rs.getString("email"));
            d.setPhone(rs.getString("phone"));
            d.setStatus(rs.getString("status"));
            d.setOrderCount(rs.getInt("order_count"));
            if (rs.getTimestamp("created_at") != null)
                d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
            return d;
        }, pat, pat, pat);
    }
}

