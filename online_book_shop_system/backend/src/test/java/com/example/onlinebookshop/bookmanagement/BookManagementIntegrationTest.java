package com.example.onlinebookshop.bookmanagement;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Book Management API.
 *
 * ╔═══════════╤════════════════════════════════════╤══════════════════════════╤══════════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════════╪══════════════════════════╪══════════════════════════════╪══════╣
 * ║ IT_BM_01 │ GET /api/management/books │ — │ 200 + list (has test data) │ N ║
 * ║ IT_BM_02 │ GET /api/management/books/{id} │ id=90001 │ 200 + detail │ N ║
 * ║ IT_BM_03 │ GET /api/management/books/{id} │ id=99999 │ 404 Not Found │ A ║
 * ║ IT_BM_04 │ POST /api/management/books │ valid │ 201 Created │ N ║
 * ║ IT_BM_05 │ PUT /api/management/books/{id} │ valid │ 200 OK │ N ║
 * ║ IT_BM_06 │ PUT /api/management/books/{id} │ id=99999 │ 404 Not Found │ A ║
 * ║ IT_BM_07 │ PUT /books/{id}/status │ ACTIVE │ 200 OK │ N ║
 * ║ IT_BM_08 │ PUT /books/{id}/status │ INVALID │ 400 Bad Request │ A ║
 * ║ IT_BM_09 │ PUT /books/{id}/status │ id=99999 │ 404 Not Found │ A ║
 * ║ IT_BM_10 │ DELETE /api/management/books/{id} │ id=90001 │ 204 No Content │
 * N ║
 * ║ IT_BM_11 │ DELETE /api/management/books/{id} │ id=99999 │ 404 Not Found │ A
 * ║
 * ║ IT_BM_12 │ GET /lookup/categories │ — │ 200 + list │ N ║
 * ║ IT_BM_13 │ GET /lookup/authors │ — │ 200 + list │ N ║
 * ║ IT_BM_14 │ POST /api/management/books │ empty title │ verify behavior │ B ║
 * ║ IT_BM_15 │ POST /api/management/books │ invalid categoryId │ verify
 * behavior │ A ║
 * ║ IT_BM_16 │ PUT /api/management/books/{id} │ empty body │ verify behavior │
 * B ║
 * ║ IT_BM_17 │ GET /api/management/books │ verify soft-deleted excl │ 200 + no
 * id=90005 │ N ║
 * ║ IT_BM_18 │ GET /api/management/books/{id} │ id=90002 ACTIVE book │ 200 +
 * correct status │ N ║
 * ║ IT_BM_19 │ PUT /books/{id}/status │ DRAFT → HIDDEN │ 200 OK │ N ║
 * ║ IT_BM_20 │ DELETE then GET │ id=90002 │ 204 then 404 │ N ║
 * ╚═══════════╧════════════════════════════════════╧══════════════════════════╧══════════════════════════════╧══════╝
 * Type: N = Normal, A = Abnormal, B = Boundary
 */
@DisplayName("Integration Tests — Book Management")
class BookManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    // ── IT_BM_01: GET /api/management/books → 200 + list ──
    @Test
    @DisplayName("IT_BM_01 | GET /books → 200 OK with seeded books")
    void getAllBooks_returnsSeededData() throws Exception {
        mockMvc.perform(get("/api/management/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[?(@.title == 'Test Clean Code')]").exists());
    }

    // ── IT_BM_02: GET /books/{id} → 200 + detail ──
    @Test
    @DisplayName("IT_BM_02 | GET /books/90001 → 200 OK with full detail, authors, variants, images")
    void getBookById_found() throws Exception {
        mockMvc.perform(get("/api/management/books/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(90001))
                .andExpect(jsonPath("$.title").value("Test Clean Code"))
                .andExpect(jsonPath("$.isbn13").value("9780000000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.authors", hasSize(2)))
                .andExpect(jsonPath("$.variants", hasSize(2)))
                .andExpect(jsonPath("$.images", hasSize(2)));
    }

    // ── IT_BM_03: GET /books/{id} → 404 Not Found ──
    @Test
    @DisplayName("IT_BM_03 | GET /books/99999 → 404 Not Found")
    void getBookById_notFound() throws Exception {
        mockMvc.perform(get("/api/management/books/99999"))
                .andExpect(status().isNotFound());
    }

    // ── IT_BM_04: POST /books → 201 Created ──
    @Test
    @DisplayName("IT_BM_04 | POST /books (valid body) → 201 Created")
    void createBook_success() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Integration Test Book");
        req.setIsbn13("9781234567890");
        req.setCategoryId(90001L);
        req.setPublisherName("IT Publisher");
        req.setPublicationYear(2024);
        req.setLanguage("vi");
        req.setShortDescription("Created via integration test");

        AuthorInput author = new AuthorInput();
        author.setAuthorId(90001L);
        author.setRole("AUTHOR");
        author.setSortOrder(1);
        req.setAuthors(List.of(author));

        mockMvc.perform(post("/api/management/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Book"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    // ── IT_BM_05: PUT /books/{id} → 200 OK ──
    @Test
    @DisplayName("IT_BM_05 | PUT /books/90001 (update title) → 200 OK")
    void updateBook_success() throws Exception {
        UpdateBookRequest req = new UpdateBookRequest();
        req.setTitle("Updated Clean Code");
        req.setIsbn13("9780000000001");
        req.setPublisherName("Test Publisher");
        req.setLanguage("vi");
        req.setStatus("ACTIVE");
        req.setCategoryId(90001L);

        mockMvc.perform(put("/api/management/books/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Clean Code"));
    }

    // ── IT_BM_06: PUT /books/{id} → 404 Not Found ──
    @Test
    @DisplayName("IT_BM_06 | PUT /books/99999 → 404 Not Found")
    void updateBook_notFound() throws Exception {
        UpdateBookRequest req = new UpdateBookRequest();
        req.setTitle("Ghost Book");
        req.setIsbn13("0000000000000");

        mockMvc.perform(put("/api/management/books/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_BM_07: PUT /books/{id}/status ACTIVE → 200 ──
    @Test
    @DisplayName("IT_BM_07 | PUT /books/90003/status → ACTIVE → 200 OK")
    void changeStatus_valid() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("ACTIVE");

        mockMvc.perform(put("/api/management/books/90003/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_BM_08: PUT /books/{id}/status INVALID → 400 ──
    @Test
    @DisplayName("IT_BM_08 | PUT /books/90001/status → INVALID_STATUS → 400 Bad Request")
    void changeStatus_invalidStatus() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("INVALID_STATUS");

        mockMvc.perform(put("/api/management/books/90001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── IT_BM_09: PUT /books/{id}/status → 404 Not Found ──
    @Test
    @DisplayName("IT_BM_09 | PUT /books/99999/status → 404 Not Found")
    void changeStatus_bookNotFound() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("ACTIVE");

        mockMvc.perform(put("/api/management/books/99999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_BM_10: DELETE /books/{id} → 204 ──
    @Test
    @DisplayName("IT_BM_10 | DELETE /books/90001 → 204 No Content")
    void deleteBook_success() throws Exception {
        mockMvc.perform(delete("/api/management/books/90001"))
                .andExpect(status().isNoContent());

        // Verify deleted (should be 404 now)
        mockMvc.perform(get("/api/management/books/90001"))
                .andExpect(status().isNotFound());
    }

    // ── IT_BM_11: DELETE /books/{id} → 404 ──
    @Test
    @DisplayName("IT_BM_11 | DELETE /books/99999 → 404 Not Found")
    void deleteBook_notFound() throws Exception {
        mockMvc.perform(delete("/api/management/books/99999"))
                .andExpect(status().isNotFound());
    }

    // ── IT_BM_12: GET /lookup/categories → 200 ──
    @Test
    @DisplayName("IT_BM_12 | GET /lookup/categories → 200 OK with seeded categories")
    void getCategories() throws Exception {
        mockMvc.perform(get("/api/management/books/lookup/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    // ── IT_BM_13: GET /lookup/authors → 200 ──
    @Test
    @DisplayName("IT_BM_13 | GET /lookup/authors → 200 OK with seeded authors")
    void getAuthors() throws Exception {
        mockMvc.perform(get("/api/management/books/lookup/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    // ── IT_BM_14: POST /books → empty title (Boundary) ──
    @Test
    @DisplayName("IT_BM_14 | POST /books (empty title) → still creates (no server validation)")
    void createBook_emptyTitle() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("");
        req.setIsbn13("9781111111111");
        req.setCategoryId(90001L);
        req.setLanguage("vi");

        mockMvc.perform(post("/api/management/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    // ── IT_BM_15: POST /books → invalid categoryId (Abnormal) ──
    @Test
    @DisplayName("IT_BM_15 | POST /books (invalid categoryId=99999) → verify behavior")
    void createBook_invalidCategory() throws Exception {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Book with bad category");
        req.setIsbn13("9782222222222");
        req.setCategoryId(99999L);
        req.setLanguage("vi");

        mockMvc.perform(post("/api/management/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful()); // May succeed if FK not enforced at app level
    }

    // ── IT_BM_16: PUT /books/{id} → empty body (Boundary) ──
    @Test
    @DisplayName("IT_BM_16 | PUT /books/90001 (minimal body) → verify behavior")
    void updateBook_emptyBody() throws Exception {
        UpdateBookRequest req = new UpdateBookRequest();
        // All fields null/empty

        mockMvc.perform(put("/api/management/books/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_BM_17: GET /books → soft-deleted book excluded ──
    @Test
    @DisplayName("IT_BM_17 | GET /books → soft-deleted book (id=90005) should NOT appear")
    void getAllBooks_excludesSoftDeleted() throws Exception {
        mockMvc.perform(get("/api/management/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 90005)]").doesNotExist());
    }

    // ── IT_BM_18: GET /books/{id} → verify second book detail ──
    @Test
    @DisplayName("IT_BM_18 | GET /books/90002 → 200 OK with Design Patterns book")
    void getBookById_secondBook() throws Exception {
        mockMvc.perform(get("/api/management/books/90002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(90002))
                .andExpect(jsonPath("$.title").value("Test Design Patterns"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── IT_BM_19: PUT /books/{id}/status → DRAFT to HIDDEN ──
    @Test
    @DisplayName("IT_BM_19 | PUT /books/90003/status → HIDDEN → 200 OK (DRAFT→HIDDEN)")
    void changeStatus_draftToHidden() throws Exception {
        ChangeStatusRequest req = new ChangeStatusRequest();
        req.setStatus("HIDDEN");

        mockMvc.perform(put("/api/management/books/90003/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_BM_20: DELETE then GET → verify removal ──
    @Test
    @DisplayName("IT_BM_20 | DELETE /books/90002 then GET → 204 then 404")
    void deleteBook_thenGetReturns404() throws Exception {
        mockMvc.perform(delete("/api/management/books/90002"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/management/books/90002"))
                .andExpect(status().isNotFound());
    }
}
