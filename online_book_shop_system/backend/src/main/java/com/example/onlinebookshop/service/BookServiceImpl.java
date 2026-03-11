package com.example.onlinebookshop.service;

import com.example.onlinebookshop.entity.BookInfo;
import com.example.onlinebookshop.entity.BookVariant;
import com.example.onlinebookshop.repository.BookInfoRepository;
import com.example.onlinebookshop.repository.BookVariantRepository;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookVariantRepository variantRepository;
    private final BookInfoRepository bookInfoRepository;

    public BookServiceImpl(BookVariantRepository variantRepository, BookInfoRepository bookInfoRepository) {
        this.variantRepository = variantRepository;
        this.bookInfoRepository = bookInfoRepository;
    }

    @Override
    public BookVariantDTO createBookVariant(BookVariantDTO dto) {
        BookInfo book = new BookInfo();
        book.setTitle(dto.getTitle());
        book.setIsbn13(dto.getIsbn());
        book.setSlug(dto.getTitle() != null ? dto.getTitle().toLowerCase().replace(" ", "-") + "-" + UUID.randomUUID().toString().substring(0, 8) : "book-" + UUID.randomUUID());
        book.setShortDescription(dto.getDescription());
        book.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        book = bookInfoRepository.save(book);

        BookVariant variant = new BookVariant();
        variant.setBook(book);
        variant.setSku(dto.getSku() != null ? dto.getSku() : "SKU-" + book.getId());
        variant.setSalePrice(dto.getSalePrice() != null ? dto.getSalePrice() : java.math.BigDecimal.ZERO);
        variant.setListPrice(dto.getListPrice() != null ? dto.getListPrice() : variant.getSalePrice());
        variant.setIsActive(true);
        variant = variantRepository.save(variant);
        return toDTO(variant);
    }

    @Override
    public List<BookVariantDTO> getAllBookVariants() {
        return variantRepository.findAllActiveWithBook().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookVariantDTO getBookVariantById(Long id) {
        BookVariant v = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return toDTO(v);
    }

    @Override
    public BookVariantDTO updateBookVariant(Long id, BookVariantDTO dto) {
        BookVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (variant.getBook() != null) {
            if (dto.getTitle() != null) variant.getBook().setTitle(dto.getTitle());
            if (dto.getDescription() != null) variant.getBook().setShortDescription(dto.getDescription());
            if (dto.getStatus() != null) variant.getBook().setStatus(dto.getStatus());
            bookInfoRepository.save(variant.getBook());
        }
        if (dto.getSalePrice() != null) variant.setSalePrice(dto.getSalePrice());
        if (dto.getListPrice() != null) variant.setListPrice(dto.getListPrice());
        if (dto.getSku() != null) variant.setSku(dto.getSku());
        return toDTO(variantRepository.save(variant));
    }

    @Override
    public void deleteBookVariant(Long id) {
        variantRepository.deleteById(id);
    }

    private BookVariantDTO toDTO(BookVariant v) {
        return BookVariantDTO.builder()
                .id(v.getId())
                .bookId(v.getBook() != null ? v.getBook().getId() : null)
                .title(v.getBook() != null ? v.getBook().getTitle() : null)
                .sku(v.getSku())
                .isbn(v.getBook() != null ? v.getBook().getIsbn13() : null)
                .salePrice(v.getSalePrice())
                .listPrice(v.getListPrice())
                .description(v.getBook() != null ? v.getBook().getShortDescription() : null)
                .status(v.getBook() != null ? v.getBook().getStatus() : null)
                .build();
    }
}
