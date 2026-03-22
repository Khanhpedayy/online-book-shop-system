package com.example.onlinebookshop;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerBookServiceImpl implements ManagerBookService {

    private final ManagerBookRepository bookRepository;

    public ManagerBookServiceImpl(ManagerBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public ManagerBook createBook(ManagerBook book) {
        return bookRepository.save(book);
    }

    @Override
    public List<ManagerBook> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public ManagerBook getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ManagerBook not found"));
    }

    @Override
    public ManagerBook updateBook(Long id, ManagerBook book) {
        ManagerBook existing = getBookById(id);
        existing.setTitle(book.getTitle());
        existing.setPrice(book.getPrice());
        existing.setDescription(book.getDescription());
        existing.setStatus(book.getStatus());
        return bookRepository.save(existing);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}

