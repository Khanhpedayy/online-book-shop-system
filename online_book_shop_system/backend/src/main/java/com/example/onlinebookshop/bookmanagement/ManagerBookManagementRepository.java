package com.example.onlinebookshop.bookmanagement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class ManagerBookManagementRepository {

    private final JdbcTemplate jdbc;

    public ManagerBookManagementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• LIST â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<BookListItemDTO> findAllBooks() {
        String sql = """
                    SELECT b.id, b.isbn13, b.title, b.subtitle, b.slug,
                           b.publisher_name, b.publication_year, b.language,
                           b.short_description, b.tags_json, b.status,
                           b.category_id, c.name AS category_name,
                           b.created_at,
                           (SELECT TOP 1 bi.url FROM book_images bi
                            WHERE bi.book_id = b.id AND bi.is_cover = 1
                              AND bi.deleted_at IS NULL
                            ORDER BY bi.sort_order) AS cover_image_url
                    FROM books b
                    LEFT JOIN categories c ON c.id = b.category_id
                    WHERE b.deleted_at IS NULL
                    ORDER BY b.created_at DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            BookListItemDTO dto = new BookListItemDTO();
            dto.setId(rs.getLong("id"));
            dto.setIsbn13(rs.getString("isbn13"));
            dto.setTitle(rs.getString("title"));
            dto.setSubtitle(rs.getString("subtitle"));
            dto.setSlug(rs.getString("slug"));
            dto.setPublisherName(rs.getString("publisher_name"));
            dto.setPublicationYear(rs.getObject("publication_year", Integer.class));
            dto.setLanguage(rs.getString("language"));
            dto.setShortDescription(rs.getString("short_description"));
            dto.setTagsJson(rs.getString("tags_json"));
            dto.setStatus(rs.getString("status"));
            dto.setCategoryId(rs.getObject("category_id", Long.class));
            dto.setCategoryName(rs.getString("category_name"));
            dto.setCoverImageUrl(rs.getString("cover_image_url"));
            dto.setCreatedAt(rs.getString("created_at"));
            return dto;
        });
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• GET BY ID â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public BookDetailDTO findBookById(Long id) {
        String sql = """
                    SELECT b.*, c.name AS category_name
                    FROM books b
                    LEFT JOIN categories c ON c.id = b.category_id
                    WHERE b.id = ? AND b.deleted_at IS NULL
                """;
        List<BookDetailDTO> list = jdbc.query(sql, (rs, rowNum) -> {
            BookDetailDTO d = new BookDetailDTO();
            d.setId(rs.getLong("id"));
            d.setIsbn13(rs.getString("isbn13"));
            d.setIsbn10(rs.getString("isbn10"));
            d.setTitle(rs.getString("title"));
            d.setSubtitle(rs.getString("subtitle"));
            d.setSlug(rs.getString("slug"));
            d.setPublisherName(rs.getString("publisher_name"));
            d.setPublicationYear(rs.getObject("publication_year", Integer.class));
            d.setLanguage(rs.getString("language"));
            d.setShortDescription(rs.getString("short_description"));
            d.setDescriptionHtml(rs.getString("description_html"));
            d.setTagsJson(rs.getString("tags_json"));
            d.setSellMode(rs.getString("sell_mode"));
            d.setStatus(rs.getString("status"));
            d.setCategoryId(rs.getObject("category_id", Long.class));
            d.setCategoryName(rs.getString("category_name"));
            d.setCreatedAt(rs.getString("created_at"));
            d.setUpdatedAt(rs.getString("updated_at"));
            return d;
        }, id);
        if (list.isEmpty())
            return null;
        BookDetailDTO book = list.get(0);

        // load authors
        book.setAuthors(findAuthorsByBookId(id));
        // load variants
        book.setVariants(findVariantsByBookId(id));
        // load images
        book.setImages(findImagesByBookId(id));

        return book;
    }

    public List<BookAuthorDTO> findAuthorsByBookId(Long bookId) {
        String sql = """
                    SELECT ba.author_id, a.name, ba.role, ba.sort_order
                    FROM book_authors ba
                    JOIN authors a ON a.id = ba.author_id
                    WHERE ba.book_id = ?
                    ORDER BY ba.sort_order
                """;
        return jdbc.query(sql, (rs, rn) -> {
            BookAuthorDTO dto = new BookAuthorDTO();
            dto.setAuthorId(rs.getLong("author_id"));
            dto.setName(rs.getString("name"));
            dto.setRole(rs.getString("role"));
            dto.setSortOrder(rs.getInt("sort_order"));
            return dto;
        }, bookId);
    }

    public List<BookVariantDTO> findVariantsByBookId(Long bookId) {
        String sql = """
                    SELECT id, sku, format, edition, language, list_price, sale_price,
                           page_count, weight_grams, is_active
                    FROM book_variants
                    WHERE book_id = ? AND deleted_at IS NULL
                    ORDER BY id
                """;
        return jdbc.query(sql, (rs, rn) -> {
            BookVariantDTO dto = new BookVariantDTO();
            dto.setId(rs.getLong("id"));
            dto.setSku(rs.getString("sku"));
            dto.setFormat(rs.getString("format"));
            dto.setEdition(rs.getString("edition"));
            dto.setLanguage(rs.getString("language"));
            dto.setListPrice(rs.getDouble("list_price"));
            dto.setSalePrice(rs.getDouble("sale_price"));
            dto.setPageCount(rs.getObject("page_count", Integer.class));
            dto.setWeightGrams(rs.getObject("weight_grams", Integer.class));
            dto.setIsActive(rs.getBoolean("is_active"));
            return dto;
        }, bookId);
    }

    public List<BookImageDTO> findImagesByBookId(Long bookId) {
        String sql = """
                    SELECT id, url, alt_text, is_cover, sort_order, variant_id
                    FROM book_images
                    WHERE book_id = ? AND deleted_at IS NULL
                    ORDER BY sort_order
                """;
        return jdbc.query(sql, (rs, rn) -> {
            BookImageDTO dto = new BookImageDTO();
            dto.setId(rs.getLong("id"));
            dto.setUrl(rs.getString("url"));
            dto.setAltText(rs.getString("alt_text"));
            dto.setIsCover(rs.getBoolean("is_cover"));
            dto.setSortOrder(rs.getInt("sort_order"));
            dto.setVariantId(rs.getObject("variant_id", Long.class));
            return dto;
        }, bookId);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• CREATE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public Long insertBook(CreateBookRequest req) {
        String slug = req.getTitle().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                + "-" + System.currentTimeMillis();

        String sql = """
                    INSERT INTO books (category_id, isbn13, isbn10, title, subtitle, slug,
                                       publisher_name, publication_year, language,
                                       short_description, description_html, tags_json,
                                       sell_mode, status, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT', SYSUTCDATETIME())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, req.getCategoryId());
            ps.setString(2, req.getIsbn13());
            ps.setString(3, req.getIsbn10());
            ps.setString(4, req.getTitle());
            ps.setString(5, req.getSubtitle());
            ps.setString(6, slug);
            ps.setString(7, req.getPublisherName());
            ps.setObject(8, req.getPublicationYear());
            ps.setString(9, req.getLanguage() != null ? req.getLanguage() : "vi");
            ps.setString(10, req.getShortDescription());
            ps.setString(11, req.getDescriptionHtml());
            ps.setString(12, req.getTagsJson());
            ps.setString(13, req.getSellMode() != null ? req.getSellMode() : "PER_COPY");
            return ps;
        }, kh);
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    public void insertBookAuthors(Long bookId, List<AuthorInput> authors) {
        if (authors == null || authors.isEmpty())
            return;
        String sql = "INSERT INTO book_authors (book_id, author_id, role, sort_order) VALUES (?, ?, ?, ?)";
        for (AuthorInput a : authors) {
            jdbc.update(sql, bookId, a.getAuthorId(),
                    a.getRole() != null ? a.getRole() : "AUTHOR",
                    a.getSortOrder() != null ? a.getSortOrder() : 0);
        }
    }

    public Long insertVariant(Long bookId, VariantInput v) {
        String sql = """
                    INSERT INTO book_variants (book_id, sku, format, edition, language,
                                               list_price, sale_price, page_count, weight_grams,
                                               is_active, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, SYSUTCDATETIME())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, bookId);
            ps.setString(2, v.getSku());
            ps.setString(3, v.getFormat());
            ps.setString(4, v.getEdition());
            ps.setString(5, v.getLanguage());
            ps.setDouble(6, v.getListPrice() != null ? v.getListPrice() : 0);
            ps.setDouble(7, v.getSalePrice() != null ? v.getSalePrice() : 0);
            ps.setObject(8, v.getPageCount());
            ps.setObject(9, v.getWeightGrams());
            return ps;
        }, kh);
        return Objects.requireNonNull(kh.getKey()).longValue();
    }

    public void insertImages(Long bookId, List<ImageInput> images) {
        if (images == null || images.isEmpty())
            return;
        String sql = """
                    INSERT INTO book_images (book_id, variant_id, url, alt_text, is_cover, sort_order, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
                """;
        for (ImageInput img : images) {
            jdbc.update(sql, bookId, img.getVariantId(), img.getUrl(), img.getAltText(),
                    img.getIsCover() != null && img.getIsCover() ? 1 : 0,
                    img.getSortOrder() != null ? img.getSortOrder() : 0);
        }
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• UPDATE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public void updateBook(Long id, UpdateBookRequest req) {
        String sql = """
                    UPDATE books SET
                        isbn13 = ?, isbn10 = ?, title = ?, subtitle = ?,
                        publisher_name = ?, publication_year = ?, language = ?,
                        short_description = ?, description_html = ?, tags_json = ?,
                        sell_mode = COALESCE(?, sell_mode), status = COALESCE(?, status), category_id = ?,
                        updated_at = SYSUTCDATETIME()
                    WHERE id = ? AND deleted_at IS NULL
                """;
        jdbc.update(sql,
                req.getIsbn13(), req.getIsbn10(), req.getTitle(), req.getSubtitle(),
                req.getPublisherName(), req.getPublicationYear(), req.getLanguage(),
                req.getShortDescription(), req.getDescriptionHtml(), req.getTagsJson(),
                req.getSellMode(), req.getStatus(), req.getCategoryId(),
                id);

        // Replace images if provided
        if (req.getImages() != null) {
            // soft-delete existing images
            jdbc.update("UPDATE book_images SET deleted_at = SYSUTCDATETIME() WHERE book_id = ? AND deleted_at IS NULL",
                    id);
            insertImages(id, req.getImages());
        }
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• STATUS CHANGE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int changeStatus(Long id, String status) {
        String sql = "UPDATE books SET status = ?, updated_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, status, id);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• SOFT DELETE â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public int softDelete(Long id) {
        String sql = "UPDATE books SET deleted_at = SYSUTCDATETIME() WHERE id = ? AND deleted_at IS NULL";
        return jdbc.update(sql, id);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• CATEGORIES (for dropdowns) â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<Map<String, Object>> findAllCategories() {
        String sql = "SELECT id, name, slug FROM categories WHERE deleted_at IS NULL AND is_active = 1 ORDER BY sort_order, name";
        return jdbc.queryForList(sql);
    }

    /* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• AUTHORS (for dropdowns) â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */

    public List<Map<String, Object>> findAllAuthors() {
        String sql = "SELECT id, name FROM authors WHERE deleted_at IS NULL ORDER BY name";
        return jdbc.queryForList(sql);
    }
}

