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

    public BookDetailDTO getBookById(Long id) {
        BookDetailDTO book = repo.findBookById(id);
        if (book == null) {
            throw new RuntimeException("ManagerBook not found with id: " + id);
        }
        return book;
    }

    @Transactional
    public BookDetailDTO createBook(CreateBookRequest req) {
        // Validate required fields
        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new IllegalArgumentException("Title is required");
        if (req.getCategoryId() == null)
            throw new IllegalArgumentException("Category is required");
        // Default language if not provided
        if (req.getLanguage() == null || req.getLanguage().isBlank())
            req.setLanguage("vi");
        // Validate sell_mode against DB CHECK constraint (PER_COPY / PER_QUANTITY)
        if (req.getSellMode() != null && !req.getSellMode().equals("PER_COPY")
                && !req.getSellMode().equals("PER_QUANTITY"))
            throw new IllegalArgumentException(
                    "Invalid sell mode: " + req.getSellMode() + ". Must be PER_COPY or PER_QUANTITY.");

        // 1) Insert main book record
        Long bookId = repo.insertBook(req);

        // 2) Insert authors
        if (req.getAuthors() != null && !req.getAuthors().isEmpty()) {
            repo.insertBookAuthors(bookId, req.getAuthors());
        }

        // 3) Insert variants
        if (req.getVariants() != null) {
            for (VariantInput v : req.getVariants()) {
                if (v.getSku() == null || v.getSku().isBlank())
                    throw new IllegalArgumentException("Variant SKU is required");
                repo.insertVariant(bookId, v);
            }
        }

        // 4) Insert images â€” skip base64 data URIs (too long for DB column)
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
        // Ensure exists
        getBookById(id);
        if (req.getTitle() != null && req.getTitle().isBlank())
            throw new IllegalArgumentException("Title cannot be empty");
        if (req.getSellMode() != null && !req.getSellMode().equals("PER_COPY")
                && !req.getSellMode().equals("PER_QUANTITY"))
            throw new IllegalArgumentException(
                    "Invalid sell mode: " + req.getSellMode() + ". Must be PER_COPY or PER_QUANTITY.");

        // Filter out base64 images before saving (DB url column can't hold base64)
        if (req.getImages() != null) {
            List<ImageInput> validImages = req.getImages().stream()
                    .filter(img -> img.getUrl() != null && !img.getUrl().startsWith("data:"))
                    .collect(Collectors.toList());
            // Only replace images if there are valid non-base64 URLs
            // If all images were base64, don't touch existing images at all
            if (validImages.isEmpty()) {
                req.setImages(null); // null = keep existing images in repo
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
        if (!status.equals("ACTIVE") && !status.equals("HIDDEN") && !status.equals("DRAFT")) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be ACTIVE, HIDDEN, or DRAFT.");
        }
        int rows = repo.changeStatus(id, status);
        if (rows == 0) {
            throw new RuntimeException("ManagerBook not found with id: " + id);
        }
    }

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

