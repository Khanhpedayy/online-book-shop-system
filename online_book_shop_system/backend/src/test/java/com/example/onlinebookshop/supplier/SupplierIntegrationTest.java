package com.example.onlinebookshop.supplier;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Supplier Management
 *
 * ╔═══════════╤════════════════════════════════╤═══════════════════════════╤══════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════╪═══════════════════════════╪══════════════════════════╪══════╣
 * ║ IT_SUP_01 │ GET /suppliers │ — │ 200 + list │ N ║
 * ║ IT_SUP_02 │ GET /suppliers/{id} │ id=90001 │ 200 + detail │ N ║
 * ║ IT_SUP_03 │ GET /suppliers/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_SUP_04 │ POST /suppliers │ valid │ 201 Created │ N ║
 * ║ IT_SUP_05 │ PUT /suppliers/{id} │ valid │ 200 OK │ N ║
 * ║ IT_SUP_06 │ PUT /suppliers/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_SUP_07 │ DELETE /suppliers/{id} │ id=90002 │ 204 No Content │ N ║
 * ║ IT_SUP_08 │ DELETE /suppliers/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_SUP_09 │ POST /suppliers │ empty name │ verify behavior │ B ║
 * ║ IT_SUP_10 │ POST /suppliers │ duplicate email │ verify behavior │ A ║
 * ║ IT_SUP_11 │ DELETE /suppliers/{id} │ id=90001 (has lots) │ verify behavior
 * │ A ║
 * ║ IT_SUP_12 │ GET /suppliers/90001 │ detail check fields │ 200 + all fields │
 * N ║
 * ║ IT_SUP_13 │ PUT /suppliers/90001 │ update all fields │ 200 + all updated │
 * N ║
 * ║ IT_SUP_14 │ GET /suppliers/90002 │ second supplier │ 200 + Beta supplier │
 * N ║
 * ╚═══════════╧════════════════════════════════╧═══════════════════════════╧══════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Supplier Management")
class SupplierIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_SUP_01 | GET /suppliers → 200 OK with seeded suppliers")
    void getAll() throws Exception {
        mockMvc.perform(get("/api/management/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("IT_SUP_02 | GET /suppliers/90001 → 200 OK")
    void getById_found() throws Exception {
        mockMvc.perform(get("/api/management/suppliers/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier Alpha"));
    }

    @Test
    @DisplayName("IT_SUP_03 | GET /suppliers/99999 → 404")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/management/suppliers/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_SUP_04 | POST /suppliers → 201 Created")
    void create() throws Exception {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("New Test Supplier");
        req.setContactPerson("Mr. New");
        req.setEmail("new@supplier.com");
        req.setPhone("0901999999");
        req.setAddress("999 New St");

        mockMvc.perform(post("/api/management/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Test Supplier"));
    }

    @Test
    @DisplayName("IT_SUP_05 | PUT /suppliers/90001 → 200 OK")
    void update_success() throws Exception {
        UpdateSupplierRequest req = new UpdateSupplierRequest();
        req.setName("Updated Alpha Supplier");
        req.setContactPerson("Mr Alpha Updated");
        req.setEmail("alpha-updated@supplier.com");

        mockMvc.perform(put("/api/management/suppliers/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Alpha Supplier"));
    }

    @Test
    @DisplayName("IT_SUP_06 | PUT /suppliers/99999 → 404")
    void update_notFound() throws Exception {
        UpdateSupplierRequest req = new UpdateSupplierRequest();
        req.setName("Ghost");

        mockMvc.perform(put("/api/management/suppliers/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_SUP_07 | DELETE /suppliers/90002 → 204 No Content")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/management/suppliers/90002"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("IT_SUP_08 | DELETE /suppliers/99999 → 404")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/management/suppliers/99999"))
                .andExpect(status().isNotFound());
    }

    // ── IT_SUP_09: POST /suppliers → empty name (Boundary) ──
    @Test
    @DisplayName("IT_SUP_09 | POST /suppliers (empty name) → verify behavior")
    void create_emptyName() throws Exception {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("");
        req.setEmail("empty@supplier.com");

        mockMvc.perform(post("/api/management/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful());
    }

    // ── IT_SUP_10: POST /suppliers → duplicate email (Abnormal) ──
    @Test
    @DisplayName("IT_SUP_10 | POST /suppliers (duplicate email) → verify behavior")
    void create_duplicateEmail() throws Exception {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("Duplicate Email Supplier");
        req.setEmail("alpha@supplier.com"); // same as 90001
        req.setPhone("0901888888");

        mockMvc.perform(post("/api/management/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful()); // No unique constraint on email at app level
    }

    // ── IT_SUP_11: DELETE /suppliers/90001 (has lots linked) ──
    @Test
    @DisplayName("IT_SUP_11 | DELETE /suppliers/90001 (has lots) → verify behavior")
    void delete_supplierWithLots() throws Exception {
        // Supplier 90001 has lots linked to it
        mockMvc.perform(delete("/api/management/suppliers/90001"))
                .andExpect(status().is2xxSuccessful());
    }

    // ── IT_SUP_12: GET /suppliers/90001 → full detail check ──
    @Test
    @DisplayName("IT_SUP_12 | GET /suppliers/90001 → 200 + full detail fields")
    void getById_fullDetailCheck() throws Exception {
        mockMvc.perform(get("/api/management/suppliers/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier Alpha"))
                .andExpect(jsonPath("$.contactPerson").value("Mr Alpha"))
                .andExpect(jsonPath("$.email").value("alpha@supplier.com"))
                .andExpect(jsonPath("$.phone").value("0281000001"));
    }

    // ── IT_SUP_13: PUT /suppliers/90001 → update all fields ──
    @Test
    @DisplayName("IT_SUP_13 | PUT /suppliers/90001 (all fields) → 200 OK")
    void update_allFields() throws Exception {
        UpdateSupplierRequest req = new UpdateSupplierRequest();
        req.setName("Fully Updated Supplier");
        req.setContactPerson("Ms Updated");
        req.setEmail("fully-updated@supplier.com");
        req.setPhone("0901777777");
        req.setAddress("777 Updated Rd");

        mockMvc.perform(put("/api/management/suppliers/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fully Updated Supplier"))
                .andExpect(jsonPath("$.contactPerson").value("Ms Updated"))
                .andExpect(jsonPath("$.email").value("fully-updated@supplier.com"));
    }

    // ── IT_SUP_14: GET /suppliers/90002 → second supplier ──
    @Test
    @DisplayName("IT_SUP_14 | GET /suppliers/90002 → 200 + Beta supplier")
    void getById_secondSupplier() throws Exception {
        mockMvc.perform(get("/api/management/suppliers/90002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier Beta"))
                .andExpect(jsonPath("$.contactPerson").value("Ms Beta"));
    }
}
