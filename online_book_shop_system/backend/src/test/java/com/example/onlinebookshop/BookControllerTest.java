package com.example.onlinebookshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooks_shouldReturn200AndList() throws Exception {
        Book book = new Book(1L, "Clean Code", "978-0132350884", 39.99, "Description", 10, "active");
        when(bookService.getAllBooks()).thenReturn(List.of(book));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].price").value(39.99));

        verify(bookService).getAllBooks();
    }

    @Test
    void getBookById_shouldReturn200AndBook() throws Exception {
        Book book = new Book(1L, "Clean Code", "978-0132350884", 39.99, "Description", 10, "active");
        when(bookService.getBookById(1L)).thenReturn(book);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        verify(bookService).getBookById(1L);
    }

    @Test
    void createBook_shouldReturn201AndCreatedBook() throws Exception {
        Book book = new Book(null, "Clean Code", "978-0132350884", 39.99, "Description", 10, "active");
        Book saved = new Book(1L, "Clean Code", "978-0132350884", 39.99, "Description", 10, "active");
        when(bookService.createBook(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        verify(bookService).createBook(any(Book.class));
    }

    @Test
    void updateBook_shouldReturn200AndUpdatedBook() throws Exception {
        Book book = new Book(1L, "Clean Code 2nd", "978-0132350884", 44.99, "Updated", 15, "active");
        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(book);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code 2nd"))
                .andExpect(jsonPath("$.price").value(44.99));

        verify(bookService).updateBook(eq(1L), any(Book.class));
    }

    @Test
    void deleteBook_shouldReturn200() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isOk());

        verify(bookService).deleteBook(1L);
    }
}
