package com.example.onlinebookshop;

import com.example.onlinebookshop.Book;
import com.example.onlinebookshop.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // CREATE
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    // READ ALL
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // READ ONE
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id,
                           @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
