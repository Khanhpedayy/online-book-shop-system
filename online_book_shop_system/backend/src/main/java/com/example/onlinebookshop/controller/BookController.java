package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.BookInfo;
import com.example.onlinebookshop.Repository.BookInfoRepository;
import com.example.onlinebookshop.Service.BookService;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookInfoRepository bookInfoRepository;

    public BookController(BookService bookService,
                          BookInfoRepository bookInfoRepository) {
        this.bookService = bookService;
        this.bookInfoRepository = bookInfoRepository;
    }

    @PostMapping
    public BookVariantDTO createBook(@RequestBody BookVariantDTO dto) {
        return bookService.createBookVariant(dto);
    }

    @GetMapping
    public List<BookVariantDTO> getAllBooks() {
        return bookService.getAllBookVariants();
    }

    @GetMapping("/{id}")
    public BookVariantDTO getBookById(@PathVariable Long id) {
        return bookService.getBookVariantById(id);
    }

    @GetMapping("/category/{id}")
    public List<BookInfo> getBooksByCategory(@PathVariable Long id) {
        return bookInfoRepository.findByCategoryId(id);
    }

    @PutMapping("/{id}")
    public BookVariantDTO updateBook(@PathVariable Long id, @RequestBody BookVariantDTO dto) {
        return bookService.updateBookVariant(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBookVariant(id);
    }
}