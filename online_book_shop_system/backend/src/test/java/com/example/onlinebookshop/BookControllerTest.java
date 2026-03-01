package com.example.onlinebookshop;

import com.example.onlinebookshop.Service.BookService;
import com.example.onlinebookshop.dto.BookVariantDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.example.onlinebookshop.Controller.BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooks_shouldReturn200AndList() throws Exception {
        BookVariantDTO dto = BookVariantDTO.builder().id(1L).title("Clean Code").salePrice(BigDecimal.valueOf(39.99)).build();
        when(bookService.getAllBookVariants()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        verify(bookService).getAllBookVariants();
    }

    @Test
    void getBookById_shouldReturn200AndDTO() throws Exception {
        BookVariantDTO dto = BookVariantDTO.builder().id(1L).title("Clean Code").salePrice(BigDecimal.valueOf(39.99)).build();
        when(bookService.getBookVariantById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        verify(bookService).getBookVariantById(1L);
    }
}
