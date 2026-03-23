package com.example.onlinebookshop.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public List<CategoryDTO> getAll() {
        return repo.findAll();
    }

    public CategoryDTO getById(Long id) {
        CategoryDTO c = repo.findById(id);
        if (c == null)
            throw new RuntimeException("Category not found: " + id);
        return c;
    }

    @Transactional
    public CategoryDTO create(CreateCategoryRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("Tên danh mục là bắt buộc");
        if (repo.existsByName(req.getName().trim(), null))
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");

        String slug = toSlug(req.getName().trim());
        Long id = repo.insert(req, slug);
        return repo.findById(id);
    }

    @Transactional
    public CategoryDTO update(Long id, UpdateCategoryRequest req) {
        if (req.getName() != null && req.getName().isBlank())
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        if (req.getName() != null && repo.existsByName(req.getName().trim(), id))
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");

        CategoryDTO existing = repo.findById(id);
        if (existing == null)
            throw new RuntimeException("Category not found: " + id);

        // Merge: keep existing values for null fields
        if (req.getName() == null) req.setName(existing.getName());
        if (req.getDescription() == null) req.setDescription(existing.getDescription());
        if (req.getSortOrder() == null) req.setSortOrder(existing.getSortOrder());
        if (req.getIsActive() == null) req.setIsActive(existing.getIsActive());

        String slug = toSlug(req.getName().trim());
        int rows = repo.update(id, req, slug);
        if (rows == 0)
            throw new RuntimeException("Category not found: " + id);
        return repo.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        CategoryDTO c = repo.findById(id);
        if (c == null)
            throw new RuntimeException("Category not found: " + id);
        if (c.getBookCount() > 0)
            throw new IllegalArgumentException(
                    "Không thể xóa danh mục đang có " + c.getBookCount() + " sách. Hãy chuyển sách sang danh mục khác trước.");
        int rows = repo.softDelete(id);
        if (rows == 0)
            throw new RuntimeException("Category not found: " + id);
    }

    /* ═══ SLUG HELPER ═══ */

    private String toSlug(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("\\p{M}", ""); // strip diacritics
        ascii = ascii.replaceAll("đ", "d").replaceAll("Đ", "D");
        return ascii.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
