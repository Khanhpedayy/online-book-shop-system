package com.example.onlinebookshop;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/books")
public class ManagerBookController {

    private final ManagerBookService bookService;

    public ManagerBookController(ManagerBookService bookService) {
        this.bookService = bookService;
    }

    // CREATE
    @PostMapping
    public ManagerBook createBook(@RequestBody ManagerBook book) {
        return bookService.createBook(book);
    }

    // READ ALL
    @GetMapping
    public List<ManagerBook> getAllBooks() {
        return bookService.getAllBooks();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ManagerBook getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ManagerBook updateBook(@PathVariable Long id,
            @RequestBody ManagerBook book) {
        return bookService.updateBook(id, book);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}

