package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.BookInfo;
import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Repository.BookInfoRepository;
import com.example.onlinebookshop.Repository.BookVariantRepository;
import com.example.onlinebookshop.dto.BookDetailDTO;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookVariantRepository variantRepository;
    private final BookInfoRepository bookInfoRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BookServiceImpl(BookVariantRepository variantRepository,
                           BookInfoRepository bookInfoRepository,
                           NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.variantRepository = variantRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
        List<BookVariant> variants = variantRepository.findAllActiveWithBook();
        Map<Long, String> coverByBookId = getCoverUrlsByBookIds(variants);
        return variants.stream()
                .map(v -> toDTO(v, coverByBookId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookVariantDTO> findBooks(String keyword,
                                          String publisherName,
                                          Double minPrice,
                                          Double maxPrice,
                                          Long categoryId,
                                          String format) {
        List<BookVariant> filtered = variantRepository.findAllActiveWithBook().stream()
                .filter(v -> matchesKeyword(v, keyword))
                .filter(v -> matchesPublisher(v, publisherName))
                .filter(v -> matchesPrice(v, minPrice, maxPrice))
                .filter(v -> matchesCategory(v, categoryId))
                .filter(v -> matchesFormat(v, format))
                .collect(Collectors.toList());
        Map<Long, String> coverByBookId = getCoverUrlsByBookIds(filtered);
        return filtered.stream()
                .map(v -> toDTO(v, coverByBookId))
                .collect(Collectors.toList());
    }

    private boolean matchesFormat(BookVariant v, String format) {
        if (format == null || format.isBlank()) {
            return true;
        }
        String f = format.trim().toUpperCase(Locale.ROOT);
        String vf = v.getFormat() != null ? v.getFormat().trim().toUpperCase(Locale.ROOT) : "";
        return vf.equals(f);
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
        String p = publisherName.toLowerCase(Locale.ROOT);
        if (v.getBook() == null) {
            return false;
        }
        String pub = v.getBook().getPublisherName() != null
                ? v.getBook().getPublisherName().toLowerCase(Locale.ROOT)
                : "";
        return pub.contains(p);
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
    public BookDetailDTO getBookDetail(Long id) {
        // UI uses /api/books/{id} where {id} is typically bookId.
        // Backward compatible: if no book found by id, treat it as variantId.
        BookInfo book = bookInfoRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null && "ACTIVE".equalsIgnoreCase(b.getStatus()))
                .orElse(null);

        if (book == null) {
            BookVariant anchor = variantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            if (anchor.getDeletedAt() != null || !Boolean.TRUE.equals(anchor.getIsActive())) {
                throw new RuntimeException("Book not found");
            }
            book = anchor.getBook();
            if (book == null || book.getDeletedAt() != null || !"ACTIVE".equalsIgnoreCase(book.getStatus())) {
                throw new RuntimeException("Book not found");
            }
        }

        String coverUrl = getCoverUrlByBookId(book.getId());
        final String[] publisherNameRef = new String[]{book.getPublisherName()};
        final Integer[] publicationYearRef = new Integer[]{book.getPublicationYear()};

        // Defensive: if JPA mapping/cache is out-of-sync, read directly from DB.
        String pn = publisherNameRef[0];
        Integer py = publicationYearRef[0];
        if ((pn == null || pn.isBlank()) && py == null) {
            String sql = """
                    SELECT TOP 1 publisher_name, publication_year
                    FROM books
                    WHERE id = :bookId AND deleted_at IS NULL
                    """;
            MapSqlParameterSource params = new MapSqlParameterSource("bookId", book.getId());
            namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (publisherNameRef[0] == null || publisherNameRef[0].isBlank()) {
                    publisherNameRef[0] = rs.getString("publisher_name");
                }
                if (publicationYearRef[0] == null) {
                    publicationYearRef[0] = rs.getObject("publication_year", Integer.class);
                }
            });
        }

        Map<Long, String> coverByBookId = Map.of(book.getId(), coverUrl);
        List<BookVariantDTO> variants = variantRepository.findActiveByBookId(book.getId()).stream()
                .map(v -> toDTO(v, coverByBookId))
                .collect(Collectors.toList());
        return BookDetailDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn13(book.getIsbn13())
                .isbn10(book.getIsbn10())
                .coverImageUrl(coverUrl)
                .publisherName(publisherNameRef[0])
                .publicationYear(publicationYearRef[0])
                .description(book.getShortDescription())
                .variants(variants)
                .build();
    }

    @Override
    public BookVariantDTO getBookVariantById(Long id) {
        BookVariant v = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        Long bookId = v.getBook() != null ? v.getBook().getId() : null;
        Map<Long, String> coverByBookId = bookId == null
                ? Map.of()
                : Map.of(bookId, getCoverUrlByBookId(bookId));
        return toDTO(v, coverByBookId);
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
        return toDTO(v, Map.of());
    }

    private BookVariantDTO toDTO(BookVariant v, Map<Long, String> coverByBookId) {
        Long bookId = v.getBook() != null ? v.getBook().getId() : null;
        return BookVariantDTO.builder()
                .id(v.getId())
                .bookId(bookId)
                .title(v.getBook() != null ? v.getBook().getTitle() : null)
                .sku(v.getSku())
                .isbn(v.getBook() != null ? v.getBook().getIsbn13() : null)
                .publisherName(v.getBook() != null ? v.getBook().getPublisherName() : null)
                .publicationYear(v.getBook() != null ? v.getBook().getPublicationYear() : null)
                .format(v.getFormat())
                .salePrice(v.getSalePrice())
                .listPrice(v.getListPrice())
                .coverImageUrl(bookId != null ? coverByBookId.get(bookId) : null)
                .description(v.getBook() != null ? v.getBook().getShortDescription() : null)
                .status(v.getBook() != null ? v.getBook().getStatus() : null)
                .build();
    }

    private Map<Long, String> getCoverUrlsByBookIds(List<BookVariant> variants) {
        Set<Long> bookIds = new LinkedHashSet<>();
        for (BookVariant variant : variants) {
            if (variant.getBook() != null && variant.getBook().getId() != null) {
                bookIds.add(variant.getBook().getId());
            }
        }
        if (bookIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT bi.book_id, bi.url
                FROM book_images bi
                INNER JOIN (
                    SELECT book_id, MIN(sort_order) AS min_sort_order
                    FROM book_images
                    WHERE deleted_at IS NULL
                      AND is_cover = 1
                      AND book_id IN (:bookIds)
                    GROUP BY book_id
                ) x ON x.book_id = bi.book_id AND x.min_sort_order = bi.sort_order
                WHERE bi.deleted_at IS NULL
                  AND bi.is_cover = 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("bookIds", bookIds);
        Map<Long, String> result = new HashMap<>();
        namedParameterJdbcTemplate.query(sql, params, rs -> {
            long bookId = rs.getLong("book_id");
            if (!rs.wasNull() && !result.containsKey(bookId)) {
                result.put(bookId, rs.getString("url"));
            }
        });
        return result;
    }

    private String getCoverUrlByBookId(Long bookId) {
        if (bookId == null) {
            return null;
        }
        String sql = """
                SELECT TOP 1 bi.url
                FROM book_images bi
                WHERE bi.book_id = :bookId
                  AND bi.deleted_at IS NULL
                  AND bi.is_cover = 1
                ORDER BY bi.sort_order
                """;
        List<String> urls = namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("bookId", bookId),
                (rs, rowNum) -> rs.getString("url")
        );
        return urls.isEmpty() ? null : urls.get(0);
    }
}
