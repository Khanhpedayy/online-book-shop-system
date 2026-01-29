package com.example.onlinebookshop;

import com.example.onlinebookshop.Book;
import com.example.onlinebookshop.BookRepository;
import com.example.onlinebookshop.BookService;
import org.springframework.stereotype   .Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Book updateBook(Long id, Book book) {
        Book existing = getBookById(id);
        existing.setTitle(book.getTitle());
        existing.setPrice(book.getPrice());
        existing.setDescription(book.getDescription());
        existing.setStockQuantity(book.getStockQuantity());
        existing.setStatus(book.getStatus());
        return bookRepository.save(existing);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
