package com.example.onlinebookshop.bookmanagement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ManagerBookManagementService {

    private final ManagerBookManagementRepository repo;

    public ManagerBookManagementService(ManagerBookManagementRepository repo) {
        this.repo = repo;
    }

    public List<BookListItemDTO> getAllBooks() {
        return repo.findAllBooks();
    }

    public List<BookListItemDTO> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return repo.searchBooksByKeyword(query);
    }

    public BookDetailDTO getBookById(Long id) {
        BookDetailDTO book = repo.findBookById(id);
        if (book == null) {
            throw new RuntimeException("ManagerBook not found with id: " + id);
        }
        return book;
    }

    // Strip dashes/spaces from ISBN and validate length
    private String sanitizeIsbn(String isbn, int maxLen, String label) {
        if (isbn == null || isbn.isBlank()) return null;
        String cleaned = isbn.replaceAll("[\\s-]", "");
        if (cleaned.length() > maxLen) {
            throw new IllegalArgumentException(label + " must be at most " + maxLen + " digits (got " + cleaned.length() + ")");
        }
        return cleaned;
    }

    @Transactional
    public BookDetailDTO createBook(CreateBookRequest req) {

        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new IllegalArgumentException("Title is required");

        if (req.getCategoryId() == null)
            throw new IllegalArgumentException("Category is required");

        if (req.getLanguage() == null || req.getLanguage().isBlank())
            req.setLanguage("vi");

        // Sanitize ISBNs (strip dashes/spaces, validate length)
        req.setIsbn13(sanitizeIsbn(req.getIsbn13(), 13, "ISBN-13"));
        req.setIsbn10(sanitizeIsbn(req.getIsbn10(), 10, "ISBN-10"));

        // validate frontend value
        if (req.getSellMode() != null && !req.getSellMode().isBlank() &&
                !req.getSellMode().equals("PER_COPY") &&
                !req.getSellMode().equals("PER_QUANTITY")) {

            throw new IllegalArgumentException(
                    "Invalid sell mode: " + req.getSellMode() + ". Must be PER_COPY or PER_QUANTITY.");
        }

        if (req.getStatus() != null && !req.getStatus().isBlank() &&
                !req.getStatus().equals("ACTIVE") &&
                !req.getStatus().equals("HIDDEN") &&
                !req.getStatus().equals("DRAFT")) {
            throw new IllegalArgumentException(
                    "Invalid status: " + req.getStatus() + ". Must be ACTIVE, HIDDEN, or DRAFT.");
        }

        // ===== MAP FRONTEND -> DATABASE =====
        if ("PER_QUANTITY".equals(req.getSellMode())) {
            req.setSellMode("QUANTITY");
        }

        Long bookId = repo.insertBook(req);

        if (req.getAuthors() != null && !req.getAuthors().isEmpty()) {
            repo.insertBookAuthors(bookId, req.getAuthors());
        }

        if (req.getVariants() != null) {
            for (VariantInput v : req.getVariants()) {
                if (v.getSku() == null || v.getSku().isBlank())
                    throw new IllegalArgumentException("Variant SKU is required");

                repo.insertVariant(bookId, v);
            }
        }

        if (req.getImages() != null && !req.getImages().isEmpty()) {
            List<ImageInput> validImages = req.getImages().stream()
                    .filter(img -> img.getUrl() != null && !img.getUrl().startsWith("data:"))
                    .collect(Collectors.toList());

            if (!validImages.isEmpty()) {
                repo.insertImages(bookId, validImages);
            }
        }

        return repo.findBookById(bookId);
    }

    @Transactional
    public BookDetailDTO updateBook(Long id, UpdateBookRequest req) {

        getBookById(id);

        if (req.getTitle() != null && req.getTitle().isBlank())
            throw new IllegalArgumentException("Title cannot be empty");

        // Sanitize ISBNs (strip dashes/spaces, validate length)
        req.setIsbn13(sanitizeIsbn(req.getIsbn13(), 13, "ISBN-13"));
        req.setIsbn10(sanitizeIsbn(req.getIsbn10(), 10, "ISBN-10"));

        if (req.getSellMode() != null && !req.getSellMode().isBlank() &&
                !req.getSellMode().equals("PER_COPY") &&
                !req.getSellMode().equals("PER_QUANTITY")) {

            throw new IllegalArgumentException(
                    "Invalid sell mode: " + req.getSellMode() + ". Must be PER_COPY or PER_QUANTITY.");
        }

        // ===== MAP FRONTEND -> DATABASE =====
        if ("PER_QUANTITY".equals(req.getSellMode())) {
            req.setSellMode("QUANTITY");
        }

        if (req.getImages() != null) {
            List<ImageInput> validImages = req.getImages().stream()
                    .filter(img -> img.getUrl() != null && !img.getUrl().startsWith("data:"))
                    .collect(Collectors.toList());

            if (validImages.isEmpty()) {
                req.setImages(null);
            } else {
                req.setImages(validImages);
            }
        }

        repo.updateBook(id, req);

        return repo.findBookById(id);
    }

    @Transactional
    public void changeStatus(Long id, String status) {

        if (status == null || status.isBlank())
            throw new IllegalArgumentException("Status is required");

        if (!status.equals("ACTIVE") &&
                !status.equals("HIDDEN") &&
                !status.equals("DRAFT")) {

            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Must be ACTIVE, HIDDEN, or DRAFT.");
        }

        int rows = repo.changeStatus(id, status);

        if (rows == 0) {
            throw new RuntimeException("ManagerBook not found with id: " + id);
        }
    }

    @Transactional
    public void deleteBook(Long id) {
        int rows = repo.softDelete(id);

        if (rows == 0) {
            throw new RuntimeException("ManagerBook not found with id: " + id);
        }
    }

    public List<Map<String, Object>> getAllCategories() {
        return repo.findAllCategories();
    }

    public List<Map<String, Object>> getAllAuthors() {
        return repo.findAllAuthors();
    }
}