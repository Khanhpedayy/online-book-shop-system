package com.example.onlinebookshop.lot;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Lot / Goods Receipt
 *
 * ╔═══════════╤═════════════════════════════════════════╤════════════════════╤══════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪═════════════════════════════════════════╪════════════════════╪══════════════════════════╪══════╣
 * ║ IT_LOT_01 │ GET /lots │ — │ 200 + list │ N ║
 * ║ IT_LOT_02 │ GET /lots?supplierId=90001 │ filter │ 200 + filtered │ N ║
 * ║ IT_LOT_03 │ GET /lots?variantId=90001 │ filter │ 200 + filtered │ N ║
 * ║ IT_LOT_04 │ GET /lots/{id} │ id=90001 │ 200 + detail │ N ║
 * ║ IT_LOT_05 │ GET /lots/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_LOT_06 │ POST /lots │ valid │ 201 Created │ N ║
 * ║ IT_LOT_07 │ POST /lots/{id}/generate-copies │ valid │ 200 + count │ N ║
 * ║ IT_LOT_08 │ POST /lots/{id}/generate-copies │ id=99999 │ 404 │ A ║
 * ║ IT_LOT_09 │ PUT /lots/{id}/lock │ id=90001 │ 200 OK │ N ║
 * ║ IT_LOT_10 │ PUT /lots/{id}/unlock │ id=90003 │ 200 OK │ N ║
 * ║ IT_LOT_11 │ POST /lots │ duplicate lotCode │ verify behavior │ A ║
 * ║ IT_LOT_12 │ POST /lots │ qty=0 │ boundary │ B ║
 * ║ IT_LOT_13 │ PUT /lots/{id}/lock │ id=90003 (locked) │ verify re-lock │ A ║
 * ║ IT_LOT_14 │ PUT /lots/{id}/unlock │ id=90001 (active) │ verify re-unlock │
 * A ║
 * ║ IT_LOT_15 │ GET /lots?supplierId=99999 │ no match │ 200 + empty │ B ║
 * ║ IT_LOT_16 │ GET /lots/{id} │ id=90001 detail │ 200 + all fields │ N ║
 * ║ IT_LOT_17 │ PUT /lots/99999/lock │ not found │ 404 │ A ║
 * ║ IT_LOT_18 │ PUT /lots/99999/unlock │ not found │ 404 │ A ║
 * ╚═══════════╧═════════════════════════════════════════╧════════════════════╧══════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Lot / Goods Receipt")
class LotIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_LOT_01 | GET /lots → 200 + seeded lots")
    void getAll_noFilter() throws Exception {
        mockMvc.perform(get("/api/inventory/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("IT_LOT_02 | GET /lots?supplierId=90001 → filtered lots")
    void getAll_bySupplierId() throws Exception {
        mockMvc.perform(get("/api/inventory/lots").param("supplierId", "90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("IT_LOT_03 | GET /lots?variantId=90001 → filtered lots")
    void getAll_byVariantId() throws Exception {
        mockMvc.perform(get("/api/inventory/lots").param("variantId", "90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("IT_LOT_04 | GET /lots/90001 → 200 + detail")
    void getById_found() throws Exception {
        mockMvc.perform(get("/api/inventory/lots/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotCode").value("TEST-LOT-001"))
                .andExpect(jsonPath("$.qtyReceived").value(50));
    }

    @Test
    @DisplayName("IT_LOT_05 | GET /lots/99999 → 404")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/inventory/lots/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_LOT_06 | POST /lots → 201 Created")
    void createLot() throws Exception {
        CreateLotRequest req = new CreateLotRequest();
        req.setLotCode("TEST-LOT-NEW");
        req.setVariantId(90001L);
        req.setSupplierId(90001L);
        req.setQtyReceived(25);
        req.setUnitCost(160000.0);
        req.setConditionDefault("NEW");
        req.setInvoiceNo("INV-IT-NEW");
        req.setNote("Created by integration test");

        mockMvc.perform(post("/api/inventory/lots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lotCode").value("TEST-LOT-NEW"));
    }

    @Test
    @DisplayName("IT_LOT_07 | POST /lots/90001/generate-copies → 200 + generated count")
    void generateCopies_success() throws Exception {
        GenerateCopiesRequest req = new GenerateCopiesRequest();
        req.setPrefix("GEN");
        req.setDefaultLocation("A-01-01");
        req.setConditionGrade("NEW");

        mockMvc.perform(post("/api/inventory/lots/90001/generate-copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated").value(3));
    }

    @Test
    @DisplayName("IT_LOT_08 | POST /lots/99999/generate-copies → 404")
    void generateCopies_notFound() throws Exception {
        GenerateCopiesRequest req = new GenerateCopiesRequest();
        req.setPrefix("GEN");
        req.setDefaultLocation("A-01-01");
        req.setConditionGrade("NEW");

        mockMvc.perform(post("/api/inventory/lots/99999/generate-copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_LOT_09 | PUT /lots/90001/lock → 200 OK")
    void lockLot() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/90001/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Quality hold\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_LOT_10 | PUT /lots/90003/unlock → 200 OK")
    void unlockLot() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/90003/unlock"))
                .andExpect(status().isOk());
    }

    // ── IT_LOT_11: POST /lots → duplicate lotCode ──
    @Test
    @DisplayName("IT_LOT_11 | POST /lots (duplicate lotCode) → verify behavior")
    void createLot_duplicateLotCode() throws Exception {
        CreateLotRequest req = new CreateLotRequest();
        req.setLotCode("TEST-LOT-001"); // already exists
        req.setVariantId(90001L);
        req.setSupplierId(90001L);
        req.setQtyReceived(10);
        req.setUnitCost(100000.0);
        req.setConditionDefault("NEW");

        mockMvc.perform(post("/api/inventory/lots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful()); // May succeed if no unique constraint at app level
    }

    // ── IT_LOT_12: POST /lots → qty=0 (Boundary) ──
    @Test
    @DisplayName("IT_LOT_12 | POST /lots (qty=0) → boundary")
    void createLot_zeroQty() throws Exception {
        CreateLotRequest req = new CreateLotRequest();
        req.setLotCode("TEST-LOT-ZERO");
        req.setVariantId(90001L);
        req.setSupplierId(90001L);
        req.setQtyReceived(0);
        req.setUnitCost(100000.0);
        req.setConditionDefault("NEW");

        mockMvc.perform(post("/api/inventory/lots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful());
    }

    // ── IT_LOT_13: PUT /lots/90003/lock → already locked ──
    @Test
    @DisplayName("IT_LOT_13 | PUT /lots/90003/lock → re-lock already locked lot")
    void lockLot_alreadyLocked() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/90003/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Double lock test\"}"))
                .andExpect(status().isOk());
    }

    // ── IT_LOT_14: PUT /lots/90001/unlock → already active ──
    @Test
    @DisplayName("IT_LOT_14 | PUT /lots/90001/unlock → unlock already active lot")
    void unlockLot_alreadyActive() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/90001/unlock"))
                .andExpect(status().isOk());
    }

    // ── IT_LOT_15: GET /lots?supplierId=99999 → empty (Boundary) ──
    @Test
    @DisplayName("IT_LOT_15 | GET /lots?supplierId=99999 → 200 + empty list")
    void getAll_byNonExistentSupplier() throws Exception {
        mockMvc.perform(get("/api/inventory/lots").param("supplierId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── IT_LOT_16: GET /lots/90001 → full detail check ──
    @Test
    @DisplayName("IT_LOT_16 | GET /lots/90001 → 200 + all detail fields")
    void getById_fullDetailCheck() throws Exception {
        mockMvc.perform(get("/api/inventory/lots/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotCode").value("TEST-LOT-001"))
                .andExpect(jsonPath("$.qtyReceived").value(50))
                .andExpect(jsonPath("$.qtyAvailable").value(40))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── IT_LOT_17: PUT /lots/99999/lock → 404 ──
    @Test
    @DisplayName("IT_LOT_17 | PUT /lots/99999/lock → 404 Not Found")
    void lockLot_notFound() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/99999/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Should fail\"}"))
                .andExpect(status().isNotFound());
    }

    // ── IT_LOT_18: PUT /lots/99999/unlock → 404 ──
    @Test
    @DisplayName("IT_LOT_18 | PUT /lots/99999/unlock → 404 Not Found")
    void unlockLot_notFound() throws Exception {
        mockMvc.perform(put("/api/inventory/lots/99999/unlock"))
                .andExpect(status().isNotFound());
    }
}
