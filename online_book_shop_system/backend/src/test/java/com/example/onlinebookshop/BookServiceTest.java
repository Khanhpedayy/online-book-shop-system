package com.example.onlinebookshop;

import com.example.onlinebookshop.Entity.BookInfo;
import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Repository.BookInfoRepository;
import com.example.onlinebookshop.Repository.BookVariantRepository;
import com.example.onlinebookshop.Service.BookServiceImpl;
import com.example.onlinebookshop.dto.BookVariantDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookVariantRepository variantRepository;

    @Mock
    private BookInfoRepository bookInfoRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookInfo bookInfo;
    private BookVariant variant;

    @BeforeEach
    void setUp() {
        bookInfo = new BookInfo(1L, null, "978-0132350884", null, "Clean Code", null,
                "clean-code", null,
                null, "ACTIVE",null, null, null, null, null);
        variant = new BookVariant(1L, bookInfo, "SKU-001", BigDecimal.valueOf(39.99),
                BigDecimal.valueOf(39.99),null, true, null, null);
    }

    @Test
    void getAllBookVariants_shouldReturnList() {
        when(variantRepository.findAllActiveWithBook()).thenReturn(List.of(variant));

        List<BookVariantDTO> result = bookService.getAllBookVariants();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        verify(variantRepository).findAllActiveWithBook();
    }

    @Test
    void getBookVariantById_whenExists_shouldReturnDTO() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        BookVariantDTO result = bookService.getBookVariantById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(variantRepository).findById(1L);
    }

    @Test
    void getBookVariantById_whenNotExists_shouldThrowException() {
        when(variantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookVariantById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }
}
