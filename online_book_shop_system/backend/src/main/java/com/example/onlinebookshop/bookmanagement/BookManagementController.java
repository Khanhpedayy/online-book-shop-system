package com.example.onlinebookshop.bookmanagement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/management/books")
@Tag(name = "2. Book Management")
public class BookManagementController {

    private final BookManagementService service;

    public BookManagementController(BookManagementService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all books", description = "Get all books with cover image, category, tags, status")
    public List<BookListItemDTO> getAllBooks() {
        return service.getAllBooks();
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by Title or ISBN")
    public List<BookListItemDTO> searchBooks(@RequestParam("q") String query) {
        return service.searchBooks(query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book detail", description = "Get full book detail with authors, variants, and images")
    public BookDetailDTO getBookById(@PathVariable("id") Long id) {
        return service.getBookById(id);
    }

    @PostMapping
    @Operation(summary = "Create book", description = "Create book with metadata (ISBN, title, author, publisher, year, category, tags, description, images)")
    public ResponseEntity<BookDetailDTO> createBook(@RequestBody CreateBookRequest req) {
        BookDetailDTO created = service.createBook(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update book information, images, and publishing status")
    public BookDetailDTO updateBook(@PathVariable("id") Long id, @RequestBody UpdateBookRequest req) {
        return service.updateBook(id, req);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Hide/Unhide book", description = "Change book status: ACTIVE (publish) | HIDDEN (hide) | DRAFT")
    public ResponseEntity<Map<String, String>> changeStatus(@PathVariable("id") Long id,
                                                            @RequestBody ChangeStatusRequest req) {
        service.changeStatus(id, req.getStatus());
        return ResponseEntity.ok(Map.of("message", "Book status changed to " + req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete book")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable("id") Long id) {
        service.deleteBook(id);
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
    }

    @GetMapping("/lookup/categories")
    @Operation(summary = "Lookup categories", description = "Get active categories for dropdown")
    public List<Map<String, Object>> getCategories() {
        return service.getAllCategories();
    }

    @GetMapping("/lookup/authors")
    @Operation(summary = "Lookup authors", description = "Get authors for dropdown")
    public List<Map<String, Object>> getAuthors() {
        return service.getAllAuthors();
    }
}

