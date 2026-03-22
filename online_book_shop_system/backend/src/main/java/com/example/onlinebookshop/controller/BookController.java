package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Service.BookService;
import com.example.onlinebookshop.dto.BookDetailDTO;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public BookVariantDTO createBook(@RequestBody BookVariantDTO dto) {
        return bookService.createBookVariant(dto);
    }

    @GetMapping
    public List<BookVariantDTO> getAllBooks() {
        return bookService.getAllBookVariants();
    }

    @GetMapping("/filter")
    public List<BookVariantDTO> getBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String publisherName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId
    ) {
        return bookService.findBooks(keyword, publisherName, minPrice, maxPrice, categoryId);
    }

    @GetMapping("/{id}")
    public BookDetailDTO getBookDetail(@PathVariable Long id) {
        return bookService.getBookDetail(id);
    }
//    @GetMapping("/{id}")
//    public BookVariantDTO getBookById(@PathVariable Long id) {
//        return bookService.getBookVariantById(id);
//    }

    @PutMapping("/{id}")
    public BookVariantDTO updateBook(@PathVariable Long id, @RequestBody BookVariantDTO dto) {
        return bookService.updateBookVariant(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBookVariant(id);
    }
}
