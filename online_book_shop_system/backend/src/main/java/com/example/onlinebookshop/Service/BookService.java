package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.dto.BookDetailDTO;
import com.example.onlinebookshop.dto.BookVariantDTO;

import java.util.List;

public interface BookService {

    BookVariantDTO createBookVariant(BookVariantDTO dto);

    List<BookVariantDTO> getAllBookVariants();

    List<BookVariantDTO> findBooks(String keyword,
                                   String publisherName,
                                   Double minPrice,
                                   Double maxPrice,
                                   Long categoryId,
                                   String format);

    BookDetailDTO getBookDetail(Long variantId);

    BookVariantDTO getBookVariantById(Long id);

    BookVariantDTO updateBookVariant(Long id, BookVariantDTO dto);

    void deleteBookVariant(Long id);
}
