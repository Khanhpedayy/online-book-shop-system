package com.example.onlinebookshop.category;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ManagerCategoryRepository {

    private final JdbcTemplate jdbc;

    public ManagerCategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ═══ LIST ALL ═══ */

    public List<CategoryDTO> findAll() {
        String sql = "SELECT c.id, c.name, c.slug, c.description, c.is_active, c.sort_order, "
                + "c.created_at, c.updated_at, "
                + "ISNULL(agg.bookCount, 0) AS bookCount "
                + "FROM categories c "
                + "LEFT JOIN ("
                + "  SELECT category_id, COUNT(*) AS bookCount "
                + "  FROM books WHERE deleted_at IS NULL GROUP BY category_id"
                + ") agg ON agg.category_id = c.id "
                + "WHERE c.deleted_at IS NULL "
                + "ORDER BY c.sort_order, c.name";
        return jdbc.query(sql, (rs, i) -> mapRow(rs));
    }

    /* ═══ GET BY ID ═══ */

    public CategoryDTO findById(Long id) {
        String sql = "SELECT c.id, c.name, c.slug, c.description, c.is_active, c.sort_order, "
                + "c.created_at, c.updated_at, "
                + "ISNULL(agg.bookCount, 0) AS bookCount "
                + "FROM categories c "
                + "LEFT JOIN ("
                + "  SELECT category_id, COUNT(*) AS bookCount "
                + "  FROM books WHERE deleted_at IS NULL GROUP BY category_id"
                + ") agg ON agg.category_id = c.id "
                + "WHERE c.id = ? AND c.deleted_at IS NULL";
        List<CategoryDTO> list = jdbc.query(sql, (rs, i) -> mapRow(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    /* ═══ INSERT ═══ */

    public Long insert(CreateCategoryRequest req, String slug) {
        String sql = "INSERT INTO categories (name, slug, description, sort_order) VALUES (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getName());
            ps.setString(2, slug);
            ps.setString(3, req.getDescription());
            ps.setObject(4, req.getSortOrder() != null ? req.getSortOrder() : 0);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    /* ═══ UPDATE ═══ */

    public int update(Long id, UpdateCategoryRequest req, String slug) {
        String sql = "UPDATE categories SET name=?, slug=?, description=?, sort_order=?, is_active=?, "
                + "updated_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL";
        return jdbc.update(sql, req.getName(), slug, req.getDescription(),
                req.getSortOrder(), req.getIsActive(), id);
    }

    /* ═══ SOFT DELETE ═══ */

    public int softDelete(Long id) {
        return jdbc.update("UPDATE categories SET deleted_at=SYSUTCDATETIME() WHERE id=? AND deleted_at IS NULL", id);
    }

    /* ═══ CHECK DUPLICATE NAME ═══ */

    public boolean existsByName(String name, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?) AND deleted_at IS NULL"
                + (excludeId != null ? " AND id != " + excludeId : "");
        Integer count = jdbc.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    /* ═══ ROW MAPPER ═══ */

    private CategoryDTO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        CategoryDTO d = new CategoryDTO();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setSlug(rs.getString("slug"));
        d.setDescription(rs.getString("description"));
        d.setIsActive(rs.getBoolean("is_active"));
        d.setSortOrder(rs.getInt("sort_order"));
        if (rs.getTimestamp("created_at") != null)
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toString());
        if (rs.getTimestamp("updated_at") != null)
            d.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString());
        d.setBookCount(rs.getInt("bookCount"));
        return d;
    }
}
