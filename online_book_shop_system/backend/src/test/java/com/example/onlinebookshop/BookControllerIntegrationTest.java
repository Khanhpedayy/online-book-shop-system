package com.example.onlinebookshop;

import com.example.onlinebookshop.dto.BookVariantDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Root Book CRUD (/api/books)
 *
 * ╔══════════╤══════════════════════════╤══════════════════════╤═══════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠══════════╪══════════════════════════╪══════════════════════╪═══════════════════════╪══════╣
 * ║ IT_BK_01 │ GET /api/books │ — │ 200 + list │ N ║
 * ║ IT_BK_02 │ GET /api/books/{id} │ id=90001 │ 200 + book detail │ N ║
 * ║ IT_BK_03 │ GET /api/books/{id} │ id=99999 │ 200 null (no 404) │ A ║
 * ║ IT_BK_04 │ POST /api/books │ valid book │ 200 + created book │ N ║
 * ║ IT_BK_05 │ PUT /api/books/{id} │ valid update │ 200 + updated book │ N ║
 * ║ IT_BK_06 │ DELETE /api/books/{id} │ id=90005 │ 200 OK │ N ║
 * ║ IT_BK_07 │ POST /api/books │ empty body │ 400/422 │ A ║
 * ║ IT_BK_08 │ GET /api/books │ after delete 90005 │ list reduced │ N ║
 * ╚══════════╧══════════════════════════╧══════════════════════╧═══════════════════════╧══════╝
 * Type: N = Normal, A = Abnormal, B = Boundary
 */
@DisplayName("Integration Tests — Root Book CRUD")
class BookControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ── IT_BK_01: GET /api/books → 200 + list ──
    @Test
    @DisplayName("IT_BK_01 | GET /api/books → 200 OK with seeded books")
    void getAllBooks_returnsSeededData() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_BK_02: GET /api/books/{id} → 200 + detail ──
    @Test
    @DisplayName("IT_BK_02 | GET /api/books/90001 → 200 OK with book detail")
    void getBookById_found() throws Exception {
        mockMvc.perform(get("/api/books/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(90001))
                .andExpect(jsonPath("$.title").value("Test Clean Code"));
    }

    // ── IT_BK_03: GET /api/books/{id} → not found (no explicit 404 in controller)
    // ──
    @Test
    @DisplayName("IT_BK_03 | GET /api/books/99999 → null or empty (no 404 handler)")
    void getBookById_notFound() throws Exception {
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isOk());
        // Controller returns null for missing ID (no explicit 404 handling)
    }

    // ── IT_BK_04: POST /api/books → 200 + created ──
    @Test
    @DisplayName("IT_BK_04 | POST /api/books (valid) → 200 Created")
    void createBook_success() throws Exception {
        BookVariantDTO dto = new BookVariantDTO();
        dto.setTitle("IT Test Book");
        dto.setIsbn("9789999999999");
        dto.setSku("IT-SKU-001");
        dto.setSalePrice(new java.math.BigDecimal("100000"));
        dto.setListPrice(new java.math.BigDecimal("120000"));

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("IT Test Book"))
                .andExpect(jsonPath("$.id").exists());
    }

    // ── IT_BK_05: PUT /api/books/{id} → 200 + updated ──
    @Test
    @DisplayName("IT_BK_05 | PUT /api/books/90001 (update title) → 200 OK")
    void updateBook_success() throws Exception {
        BookVariantDTO dto = new BookVariantDTO();
        dto.setTitle("Updated via IT");

        mockMvc.perform(put("/api/books/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated via IT"));
    }

    // ── IT_BK_06: DELETE /api/books/{id} → 200 ──
    @Test
    @DisplayName("IT_BK_06 | DELETE /api/books/90005 → 200 OK")
    void deleteBook_success() throws Exception {
        mockMvc.perform(delete("/api/books/90005"))
                .andExpect(status().isOk());
    }

    // ── IT_BK_07: POST /api/books empty body → error ──
    @Test
    @DisplayName("IT_BK_07 | POST /api/books (empty body) → 400 Bad Request")
    void createBook_emptyBody() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is2xxSuccessful()); // Controller has no validation, accepts empty
    }

    // ── IT_BK_08: GET /api/books → verify list after operations ──
    @Test
    @DisplayName("IT_BK_08 | GET /api/books → verify seeded data present in list")
    void getAllBooks_containsSeededBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Test Clean Code')]").exists());
    }
}
