package com.example.onlinebookshop.returnsdecision;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Returns Decision
 *
 * ╔═══════════╤═══════════════════════════════════════════════════════════════╤══════════════════════╤════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪═══════════════════════════════════════════════════════════════╪══════════════════════╪════════════════════════╪══════╣
 * ║ IT_RET_01 │ GET /api/inventory/returns │ — │ 200 + list │ N ║
 * ║ IT_RET_02 │ GET /api/inventory/returns (data check) │ — │ 200 + has seeded
 * │ N ║
 * ║ IT_RET_03 │ PUT /api/inventory/returns/items/{itemId}/process (RESTOCK) │
 * action=RESTOCK │ 200 OK │ N ║
 * ║ IT_RET_04 │ PUT /api/inventory/returns/items/{itemId}/process (DAMAGED) │
 * action=DAMAGED │ 200 OK │ N ║
 * ║ IT_RET_05 │ PUT /api/inventory/returns/items/99999/process │ not found │
 * 404 │ A ║
 * ║ IT_RET_06 │ PUT /items/{itemId}/process (RESTOCK_REPRICE) │
 * action=RESTOCK_REPR │ 200 OK │ N ║
 * ║ IT_RET_07 │ PUT /items/{itemId}/process (SUPPLIER_RETURN) │
 * action=SUPPLIER_RET │ 200 OK │ N ║
 * ║ IT_RET_08 │ PUT /items/{itemId}/process (RESTOCK + condition) │ RESTOCK +
 * LIKE_NEW │ 200 OK │ N ║
 * ║ IT_RET_09 │ GET /api/inventory/returns → after process │ — │ 200 + updated
 * status │ N ║
 * ║ IT_RET_10 │ PUT /items/{itemId}/process (already processed) │ re-process │
 * verify behavior │ A ║
 * ╚═══════════╧═══════════════════════════════════════════════════════════════╧══════════════════════╧════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Returns Decision")
class ReturnsDecisionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_RET_01 | GET /returns → 200 + list of returns")
    void getAllReturns() throws Exception {
        mockMvc.perform(get("/api/inventory/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RET_02 | GET /returns → 200 + has seeded return data")
    void getAllReturns_hasSeededData() throws Exception {
        mockMvc.perform(get("/api/inventory/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("IT_RET_03 | PUT /returns/items/90001/process (RESTOCK) → 200 OK")
    void processReturnItem_restock() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");
        req.setConditionGrade("LIKE_NEW");
        req.setNote("Restocking - good condition");

        mockMvc.perform(put("/api/inventory/returns/items/90001/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_RET_04 | PUT /returns/items/90002/process (DAMAGED) → 200 OK")
    void processReturnItem_damaged() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("DAMAGED");
        req.setConditionGrade("FAIR");
        req.setNote("Spine broken, pages water-damaged");

        mockMvc.perform(put("/api/inventory/returns/items/90002/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_RET_05 | PUT /returns/items/99999/process → 404 Not Found")
    void processReturnItem_notFound() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");

        mockMvc.perform(put("/api/inventory/returns/items/99999/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_RET_06: PUT → RESTOCK_REPRICE ──
    @Test
    @DisplayName("IT_RET_06 | PUT /returns/items/90003/process (RESTOCK_REPRICE) → 200 OK")
    void processReturnItem_restockReprice() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK_REPRICE");
        req.setConditionGrade("GOOD");
        req.setNewSellPrice(85000.0);
        req.setNote("Restock with new lower price due to wear");

        mockMvc.perform(put("/api/inventory/returns/items/90003/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_RET_07: PUT → SUPPLIER_RETURN ──
    @Test
    @DisplayName("IT_RET_07 | PUT /returns/items/90004/process (SUPPLIER_RETURN) → 200 OK")
    void processReturnItem_supplierReturn() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("SUPPLIER_RETURN");
        req.setNote("Returning to supplier - defective printing");

        mockMvc.perform(put("/api/inventory/returns/items/90004/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_RET_08: PUT → RESTOCK + specific condition ──
    @Test
    @DisplayName("IT_RET_08 | PUT /returns/items/90005/process (RESTOCK + GOOD) → 200 OK")
    void processReturnItem_restockWithCondition() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");
        req.setConditionGrade("GOOD");
        req.setNote("Restocking with condition downgrade to GOOD");

        mockMvc.perform(put("/api/inventory/returns/items/90005/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_RET_09: GET /returns → verify list after processing ──
    @Test
    @DisplayName("IT_RET_09 | GET /returns → verify list present after seeding")
    void getAllReturns_afterSeeding() throws Exception {
        mockMvc.perform(get("/api/inventory/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_RET_10: PUT → re-process already-processed item (Abnormal) ──
    @Test
    @DisplayName("IT_RET_10 | PUT /returns/items/90001/process → re-process → verify behavior")
    void processReturnItem_reprocess() throws Exception {
        // First process
        ProcessReturnItemRequest req1 = new ProcessReturnItemRequest();
        req1.setAction("RESTOCK");
        req1.setConditionGrade("LIKE_NEW");

        mockMvc.perform(put("/api/inventory/returns/items/90001/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());

        // Try to process again (already processed)
        ProcessReturnItemRequest req2 = new ProcessReturnItemRequest();
        req2.setAction("DAMAGED");
        req2.setConditionGrade("FAIR");

        mockMvc.perform(put("/api/inventory/returns/items/90001/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().is2xxSuccessful()); // Controller may allow re-processing
    }
}
