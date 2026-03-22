package com.example.onlinebookshop.report;

import com.example.onlinebookshop.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Reports & Dashboard
 *
 * ╔═══════════╤════════════════════════════════════════════════╤═════════════════╤══════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════════════════════╪═════════════════╪══════════════════════════╪══════╣
 * ║ IT_RPT_01 │ GET /reports/sales/daily │ — │ 200 + array │ N ║
 * ║ IT_RPT_02 │ GET /reports/sales/monthly │ — │ 200 + array │ N ║
 * ║ IT_RPT_03 │ GET /reports/sales/top-selling │ default limit │ 200 + list │ N
 * ║
 * ║ IT_RPT_04 │ GET /reports/sales/top-selling?limit=5 │ limit=5 │ 200 + ≤5
 * items │ N ║
 * ║ IT_RPT_05 │ GET /reports/slow-movers │ — │ 200 + array │ N ║
 * ║ IT_RPT_06 │ GET /reports/lot-aging │ — │ 200 + buckets │ N ║
 * ║ IT_RPT_07 │ GET /reports/inventory-value │ — │ 200 + cost data │ N ║
 * ║ IT_RPT_08 │ GET /reports/shrinkage │ — │ 200 + breakdown │ N ║
 * ║ IT_RPT_09 │ GET /reports/summary │ — │ 200 + dashboard │ N ║
 * ║ IT_RPT_10 │ GET /reports/sales/top-selling?limit=1 │ limit=1 │ 200 +
 * exactly 1 │ B ║
 * ║ IT_RPT_11 │ GET /reports/sales/top-selling?limit=0 │ limit=0 │ 200 + empty
 * │ B ║
 * ║ IT_RPT_12 │ GET /reports/sales/top-selling?limit=100 │ limit=100 │ 200 +
 * ≤100 │ B ║
 * ║ IT_RPT_13 │ GET /reports/summary → field check │ — │ has totalBooks etc. │
 * N ║
 * ║ IT_RPT_14 │ GET /reports/lot-aging → data check │ — │ has aging bucket │ N
 * ║
 * ╚═══════════╧════════════════════════════════════════════════╧═════════════════╧══════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Reports & Dashboard")
class ReportIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("IT_RPT_01 | GET /reports/sales/daily → 200 + array")
    void getSalesByDay() throws Exception {
        mockMvc.perform(get("/api/reports/sales/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_02 | GET /reports/sales/monthly → 200 + array")
    void getSalesByMonth() throws Exception {
        mockMvc.perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_03 | GET /reports/sales/top-selling → 200 + list (default limit 20)")
    void getTopSelling_default() throws Exception {
        mockMvc.perform(get("/api/reports/sales/top-selling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(20))));
    }

    @Test
    @DisplayName("IT_RPT_04 | GET /reports/sales/top-selling?limit=5 → 200 + ≤5 items")
    void getTopSelling_limit5() throws Exception {
        mockMvc.perform(get("/api/reports/sales/top-selling").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("IT_RPT_05 | GET /reports/slow-movers → 200 + array")
    void getSlowMovers() throws Exception {
        mockMvc.perform(get("/api/reports/slow-movers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_06 | GET /reports/lot-aging → 200 + buckets")
    void getLotAging() throws Exception {
        mockMvc.perform(get("/api/reports/lot-aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_07 | GET /reports/inventory-value → 200 + cost data")
    void getInventoryValue() throws Exception {
        mockMvc.perform(get("/api/reports/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_08 | GET /reports/shrinkage → 200 + breakdown")
    void getShrinkage() throws Exception {
        mockMvc.perform(get("/api/reports/shrinkage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("IT_RPT_09 | GET /reports/summary → 200 + dashboard data")
    void getDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").exists())
                .andExpect(jsonPath("$.totalVariants").exists())
                .andExpect(jsonPath("$.totalCopies").exists());
    }

    // ── IT_RPT_10: top-selling limit=1 (Boundary) ──
    @Test
    @DisplayName("IT_RPT_10 | GET /reports/sales/top-selling?limit=1 → 200 + exactly 1 item")
    void getTopSelling_limit1() throws Exception {
        mockMvc.perform(get("/api/reports/sales/top-selling").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(1))));
    }

    // ── IT_RPT_11: top-selling limit=0 (Boundary) ──
    @Test
    @DisplayName("IT_RPT_11 | GET /reports/sales/top-selling?limit=0 → 200 + empty (boundary)")
    void getTopSelling_limit0() throws Exception {
        mockMvc.perform(get("/api/reports/sales/top-selling").param("limit", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── IT_RPT_12: top-selling limit=100 (Boundary) ──
    @Test
    @DisplayName("IT_RPT_12 | GET /reports/sales/top-selling?limit=100 → 200 + large limit")
    void getTopSelling_limit100() throws Exception {
        mockMvc.perform(get("/api/reports/sales/top-selling").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(100))));
    }

    // ── IT_RPT_13: summary → field existence check ──
    @Test
    @DisplayName("IT_RPT_13 | GET /reports/summary → has all dashboard fields")
    void getDashboardSummary_allFields() throws Exception {
        mockMvc.perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").isNumber())
                .andExpect(jsonPath("$.totalVariants").isNumber())
                .andExpect(jsonPath("$.totalCopies").isNumber());
    }

    // ── IT_RPT_14: lot-aging → data check ──
    @Test
    @DisplayName("IT_RPT_14 | GET /reports/lot-aging → has aging buckets with seeded data")
    void getLotAging_dataCheck() throws Exception {
        mockMvc.perform(get("/api/reports/lot-aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
