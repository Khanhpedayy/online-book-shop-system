package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.BookInfo;
import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Repository.BookInfoRepository;
import com.example.onlinebookshop.Repository.BookVariantRepository;
import com.example.onlinebookshop.dto.BookDetailDTO;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
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
    @Transactional(readOnly = true)
    public List<BookVariantDTO> findBooks(String keyword, String publisherName, Double minPrice, Double maxPrice, Long categoryId) {
        return variantRepository.findAllActiveWithBook().stream()
                .filter(v -> matchesKeyword(v, keyword))
                .filter(v -> matchesPublisher(v, publisherName))
                .filter(v -> matchesPrice(v, minPrice, maxPrice))
                .filter(v -> matchesCategory(v, categoryId))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesCategory(BookVariant v, Long categoryId) {
        if (categoryId == null) {
            return true;
        }
        if (v.getBook() == null || v.getBook().getCategoryId() == null) {
            return false;
        }
        return categoryId.equals(v.getBook().getCategoryId());
    }

    private boolean matchesKeyword(BookVariant v, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.toLowerCase(Locale.ROOT);
        String title = v.getBook() != null && v.getBook().getTitle() != null
                ? v.getBook().getTitle().toLowerCase(Locale.ROOT) : "";
        String sku = v.getSku() != null ? v.getSku().toLowerCase(Locale.ROOT) : "";
        return title.contains(k) || sku.contains(k);
    }

    private boolean matchesPublisher(BookVariant v, String publisherName) {
        if (publisherName == null || publisherName.isBlank()) {
            return true;
        }
        // books table has no publisher column; treat filter as loose match on title/description
        String p = publisherName.toLowerCase(Locale.ROOT);
        if (v.getBook() == null) {
            return false;
        }
        String title = v.getBook().getTitle() != null ? v.getBook().getTitle().toLowerCase(Locale.ROOT) : "";
        String desc = v.getBook().getShortDescription() != null
                ? v.getBook().getShortDescription().toLowerCase(Locale.ROOT) : "";
        return title.contains(p) || desc.contains(p);
    }

    private boolean matchesPrice(BookVariant v, Double minPrice, Double maxPrice) {
        BigDecimal price = v.getSalePrice() != null ? v.getSalePrice() : BigDecimal.ZERO;
        if (minPrice != null && price.compareTo(BigDecimal.valueOf(minPrice)) < 0) {
            return false;
        }
        if (maxPrice != null && price.compareTo(BigDecimal.valueOf(maxPrice)) > 0) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookDetail(Long variantId) {
        BookVariant anchor = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (anchor.getDeletedAt() != null || !Boolean.TRUE.equals(anchor.getIsActive())) {
            throw new RuntimeException("Book not found");
        }
        BookInfo book = anchor.getBook();
        if (book == null || book.getDeletedAt() != null || !"ACTIVE".equalsIgnoreCase(book.getStatus())) {
            throw new RuntimeException("Book not found");
        }
        List<BookVariantDTO> variants = variantRepository.findActiveByBookId(book.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return BookDetailDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn13(book.getIsbn13())
                .publisherName(null)
                .publicationYear(null)
                .description(book.getShortDescription())
                .variants(variants)
                .build();
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
