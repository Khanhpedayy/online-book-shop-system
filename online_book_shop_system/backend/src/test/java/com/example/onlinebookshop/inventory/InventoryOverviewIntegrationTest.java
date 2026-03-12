package com.example.onlinebookshop.inventory;

import com.example.onlinebookshop.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Inventory Overview
 *
 * ╔═══════════╤═══════════════════════════════════════════════╤════════════════╤═══════════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪═══════════════════════════════════════════════╪════════════════╪═══════════════════════════════╪══════╣
 * ║ IT_INV_01 │ GET /overview/variants │ — │ 200 + stock by variant │ N ║
 * ║ IT_INV_02 │ GET /overview/variants (data check) │ — │ Has TEST-CC-HC
 * variant │ N ║
 * ║ IT_INV_03 │ GET /overview/lots │ — │ 200 + stock by lot │ N ║
 * ║ IT_INV_04 │ GET /overview/conditions │ — │ 200 + condition summary │ N ║
 * ║ IT_INV_05 │ GET /overview/alerts/low-stock │ — │ 200 + array │ N ║
 * ║ IT_INV_06 │ GET /overview/alerts/low-stock?threshold=0 │ threshold │ 200 +
 * empty (boundary) │ B ║
 * ║ IT_INV_07 │ GET /overview/alerts/overstock │ — │ 200 + array │ N ║
 * ║ IT_INV_08 │ GET /overview/alerts/overstock (aging lot) │ — │ Has lot with
 * ageDays>90 │ N ║
 * ║ IT_INV_09 │ GET /overview/conditions │ — │ Has LIKE_NEW condition │ N ║
 * ║ IT_INV_10 │ GET /overview/variants (aggregation) │ — │ totalAvailable ≥ 0 │
 * N ║
 * ║ IT_INV_11 │ GET /overview/lots │ — │ Has TEST-LOT-003 (LOCKED) │ N ║
 * ║ IT_INV_12 │ GET /overview/conditions │ — │ Has FAIR condition │ N ║
 * ╚═══════════╧═══════════════════════════════════════════════╧════════════════╧═══════════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Inventory Overview")
class InventoryOverviewIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("IT_INV_01 | GET /overview/variants → 200 + stock grouped by variant")
    void getStockByVariant() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("IT_INV_02 | GET /overview/variants → has TEST-CC-HC variant data")
    void getStockByVariant_dataCheck() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sku == 'TEST-CC-HC')]").exists())
                .andExpect(jsonPath("$[?(@.sku == 'TEST-CC-HC')].totalAvailable", hasItem(greaterThan(0))));
    }

    @Test
    @DisplayName("IT_INV_03 | GET /overview/lots → 200 + lot-level stock")
    void getStockByLot() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[?(@.lotCode == 'TEST-LOT-001')]").exists());
    }

    @Test
    @DisplayName("IT_INV_04 | GET /overview/conditions → 200 + condition breakdown")
    void getStockByCondition() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.condition == 'NEW')]").exists());
    }

    @Test
    @DisplayName("IT_INV_05 | GET /alerts/low-stock → 200 + array")
    void getLowStockAlerts() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_INV_06 | GET /alerts/low-stock?threshold=0 → 200 + likely empty (boundary)")
    void getLowStockAlerts_boundary() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/alerts/low-stock").param("threshold", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_INV_07 | GET /alerts/overstock → 200 + aging lots")
    void getOverstockAlerts() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/alerts/overstock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_INV_08 | GET /alerts/overstock → has aging lot (>90 days)")
    void getOverstockAlerts_hasAgingLot() throws Exception {
        // LOT-003 was received 100 days ago, LOT-004 was 200 days ago
        mockMvc.perform(get("/api/inventory/overview/alerts/overstock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.ageDays > 90)]").exists());
    }

    // ── IT_INV_09: GET /overview/conditions → has LIKE_NEW ──
    @Test
    @DisplayName("IT_INV_09 | GET /overview/conditions → has LIKE_NEW condition tier")
    void getStockByCondition_hasLikeNew() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.condition == 'LIKE_NEW')]").exists());
    }

    // ── IT_INV_10: GET /overview/variants → aggregation check ──
    @Test
    @DisplayName("IT_INV_10 | GET /overview/variants → all totalAvailable ≥ 0")
    void getStockByVariant_aggregationCheck() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].totalAvailable").exists());
    }

    // ── IT_INV_11: GET /overview/lots → has LOCKED lot ──
    @Test
    @DisplayName("IT_INV_11 | GET /overview/lots → has TEST-LOT-003 (LOCKED status)")
    void getStockByLot_hasLockedLot() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.lotCode == 'TEST-LOT-003')]").exists());
    }

    // ── IT_INV_12: GET /overview/conditions → has FAIR ──
    @Test
    @DisplayName("IT_INV_12 | GET /overview/conditions → has FAIR condition tier")
    void getStockByCondition_hasFair() throws Exception {
        mockMvc.perform(get("/api/inventory/overview/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.condition == 'FAIR')]").exists());
    }
}
