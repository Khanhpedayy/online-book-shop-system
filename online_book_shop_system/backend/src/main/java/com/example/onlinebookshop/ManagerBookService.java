package com.example.onlinebookshop;

import com.example.onlinebookshop.ManagerBook;
import java.util.List;

public interface ManagerBookService {

    ManagerBook createBook(ManagerBook book);

    List<ManagerBook> getAllBooks();

    ManagerBook getBookById(Long id);

    ManagerBook updateBook(Long id, ManagerBook book);

    void deleteBook(Long id);
}

