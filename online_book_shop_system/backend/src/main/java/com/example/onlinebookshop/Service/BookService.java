package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.dto.BookDetailDTO;
import com.example.onlinebookshop.dto.BookVariantDTO;

import java.util.List;

public interface BookService {

    BookVariantDTO createBookVariant(BookVariantDTO dto);

    List<BookVariantDTO> getAllBookVariants();

    BookVariantDTO getBookVariantById(Long id);

    BookVariantDTO updateBookVariant(Long id, BookVariantDTO dto);

    BookDetailDTO getBookDetail(Long id);

    void deleteBookVariant(Long id);
}
