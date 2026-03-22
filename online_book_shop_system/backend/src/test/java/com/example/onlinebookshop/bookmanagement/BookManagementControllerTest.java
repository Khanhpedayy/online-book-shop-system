package com.example.onlinebookshop.bookmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Book Management Controller Tests")
class BookManagementControllerTest {

    @Mock
    private BookManagementService service;

    @InjectMocks
    private BookManagementController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    /* ── GET /api/management/books ── */

    @Test
    @DisplayName("GET /books → 200 OK list")
    void getAllBooks_returnsOk() throws Exception {
        BookListItemDTO dto = new BookListItemDTO();
        dto.setId(1L);
        dto.setTitle("Clean Code");
        when(service.getAllBooks()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/management/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    /* ── GET /api/management/books/{id} ── */

    @Test
    @DisplayName("GET /books/1 → 200 OK when found")
    void getBookById_found_returnsOk() throws Exception {
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(1L);
        dto.setTitle("Clean Code");
        when(service.getBookById(1L)).thenReturn(dto);

        mockMvc().perform(get("/api/management/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books/999 → 404 when not found")
    void getBookById_notFound_returns404() throws Exception {
        when(service.getBookById(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/management/books/999"))
                .andExpect(status().isNotFound());
    }

    /* ── POST /api/management/books ── */

    @Test
    @DisplayName("POST /books → 201 Created")
    void createBook_returnsCreated() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("New Book");
        req.setIsbn13("9781234567890");

        BookDetailDTO created = new BookDetailDTO();
        created.setId(10L);
        created.setTitle("New Book");
        when(service.createBook(any())).thenReturn(created);

        mockMvc().perform(post("/api/management/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    /* ── PUT /api/management/books/{id} ── */

    @Test
    @DisplayName("PUT /books/1 → 200 OK when found")
    void updateBook_found_returnsOk() throws Exception {
        UpdateBookRequest req = new UpdateBookRequest();
        req.setTitle("Updated Title");

        BookDetailDTO updated = new BookDetailDTO();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        when(service.updateBook(eq(1L), any())).thenReturn(updated);

        mockMvc().perform(put("/api/management/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("PUT /books/999 → 404 when not found")
    void updateBook_notFound_returns404() throws Exception {
        when(service.updateBook(eq(999L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(put("/api/management/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    /* ── PUT /api/management/books/{id}/status ── */

    @Test
    @DisplayName("PUT /books/1/status → 200 OK valid status")
    void changeStatus_valid_returnsOk() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("HIDDEN");
        doNothing().when(service).changeStatus(1L, "HIDDEN");

        mockMvc().perform(put("/api/management/books/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /books/1/status → 400 invalid status")
    void changeStatus_invalid_returnsBadRequest() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("INVALID");
        doThrow(new IllegalArgumentException("Invalid status"))
                .when(service).changeStatus(1L, "INVALID");

        mockMvc().perform(put("/api/management/books/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /books/999/status → 404 not found")
    void changeStatus_notFound_returns404() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("ACTIVE");
        doThrow(new RuntimeException("Not found"))
                .when(service).changeStatus(999L, "ACTIVE");

        mockMvc().perform(put("/api/management/books/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    /* ── DELETE /api/management/books/{id} ── */

    @Test
    @DisplayName("DELETE /books/1 → 204 No Content")
    void deleteBook_found_returnsNoContent() throws Exception {
        doNothing().when(service).deleteBook(1L);

        mockMvc().perform(delete("/api/management/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /books/999 → 404 not found")
    void deleteBook_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Not found")).when(service).deleteBook(999L);

        mockMvc().perform(delete("/api/management/books/999"))
                .andExpect(status().isNotFound());
    }

    /* ── GET /api/management/books/lookup/* ── */

    @Test
    @DisplayName("GET /books/lookup/categories → 200 OK")
    void getCategories_returnsOk() throws Exception {
        when(service.getAllCategories()).thenReturn(List.of(
                Map.of("id", 1, "name", "Fiction"),
                Map.of("id", 2, "name", "Science")));

        mockMvc().perform(get("/api/management/books/lookup/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fiction"))
                .andExpect(jsonPath("$[1].name").value("Science"));
    }

    @Test
    @DisplayName("GET /books/lookup/authors → 200 OK")
    void getAuthors_returnsOk() throws Exception {
        when(service.getAllAuthors()).thenReturn(List.of(
                Map.of("id", 1, "name", "Robert Martin")));

        mockMvc().perform(get("/api/management/books/lookup/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Robert Martin"));
    }
}
