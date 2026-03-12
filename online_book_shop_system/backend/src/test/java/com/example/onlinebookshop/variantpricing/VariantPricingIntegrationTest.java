package com.example.onlinebookshop.variantpricing;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Variant & Pricing
 *
 * ╔═══════════╤════════════════════════════════════════════════════════╤══════════════════════╤══════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════════════════════════════╪══════════════════════╪══════════════════════════╪══════╣
 * ║ IT_VP_01 │ GET /variants?bookId=90001 │ bookId=90001 │ 200 + variants │ N ║
 * ║ IT_VP_02 │ GET /variants │ — │ 200 + all variants │ N ║
 * ║ IT_VP_03 │ GET /variants/{id} │ id=90001 │ 200 + detail │ N ║
 * ║ IT_VP_04 │ GET /variants/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_VP_05 │ POST /variants │ valid │ 201 Created │ N ║
 * ║ IT_VP_06 │ PUT /variants/{id} │ valid │ 200 OK │ N ║
 * ║ IT_VP_07 │ PUT /variants/{id}/price │ valid │ 200 + updated price │ N ║
 * ║ IT_VP_08 │ PUT /variants/{id}/condition-prices │ JSON tiers │ 200 OK │ N ║
 * ║ IT_VP_09 │ DELETE /variants/{id} │ id=90003 │ 204 No Content │ N ║
 * ║ IT_VP_10 │ PUT /copies/{copyId}/price-override │ override price │ 200 OK │
 * N ║
 * ║ IT_VP_11 │ GET /variants/{variantId}/copies │ variantId=90001 │ 200 + copy
 * list │ N ║
 * ║ IT_VP_12 │ PUT /variants/99999 │ not found │ 404 │ A ║
 * ║ IT_VP_13 │ DELETE /variants/99999 │ not found │ 404 │ A ║
 * ║ IT_VP_14 │ PUT /copies/99999/price-override │ not found │ 404 │ A ║
 * ║ IT_VP_15 │ POST /variants (duplicate SKU) │ duplicate │ 400 │ A ║
 * ║ IT_VP_16 │ PUT /variants/{id}/price (salePrice > listPrice) │ boundary │
 * 400 │ B ║
 * ║ IT_VP_17 │ PUT /variants/99999/price │ not found │ 404 │ A ║
 * ║ IT_VP_18 │ PUT /copies/{copyId}/price-override (null → clear) │ null price
 * │ 200 OK │ B ║
 * ║ IT_VP_19 │ GET /variants?bookId=99999 │ no book │ 200 + empty │ B ║
 * ║ IT_VP_20 │ GET /variants/90001 → field check │ detail │ 200 + all fields │
 * N ║
 * ╚═══════════╧════════════════════════════════════════════════════════╧══════════════════════╧══════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Variant & Pricing")
class VariantPricingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_VP_01 | GET /variants?bookId=90001 → 200 + variants for book")
    void getVariantsByBook() throws Exception {
        mockMvc.perform(get("/api/management/variants").param("bookId", "90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("IT_VP_02 | GET /variants → 200 + all variants")
    void getAllVariants() throws Exception {
        mockMvc.perform(get("/api/management/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("IT_VP_03 | GET /variants/90001 → 200 + detail")
    void getVariantById_found() throws Exception {
        mockMvc.perform(get("/api/management/variants/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(90001))
                .andExpect(jsonPath("$.sku").value("TEST-CC-HC"));
    }

    @Test
    @DisplayName("IT_VP_04 | GET /variants/99999 → 404")
    void getVariantById_notFound() throws Exception {
        mockMvc.perform(get("/api/management/variants/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_VP_05 | POST /variants → 201 Created")
    void createVariant() throws Exception {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setBookId(90001L);
        req.setSku("TEST-CC-AUDIO");
        req.setFormat("AUDIOBOOK");
        req.setEdition("1st");
        req.setLanguage("vi");

        mockMvc.perform(post("/api/management/variants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("TEST-CC-AUDIO"));
    }

    @Test
    @DisplayName("IT_VP_06 | PUT /variants/90001 → 200 OK")
    void updateVariant() throws Exception {
        UpdateVariantRequest req = new UpdateVariantRequest();
        req.setEdition("2nd Revised");
        req.setLanguage("en");

        mockMvc.perform(put("/api/management/variants/90001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_VP_07 | PUT /variants/90001/price → 200 + set base price")
    void setBasePrice() throws Exception {
        SetBasePriceRequest req = new SetBasePriceRequest();
        req.setListPrice(200000.0);
        req.setSalePrice(180000.0);

        mockMvc.perform(put("/api/management/variants/90001/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_VP_08 | PUT /variants/90001/condition-prices → 200 + set tiers")
    void setConditionPrices() throws Exception {
        SetConditionPricesRequest req = new SetConditionPricesRequest();
        req.setConditionPricesJson("{\"LIKE_NEW\":170000,\"GOOD\":150000,\"FAIR\":120000}");

        mockMvc.perform(put("/api/management/variants/90001/condition-prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_VP_09 | DELETE /variants/90003 → 204 No Content")
    void deleteVariant() throws Exception {
        mockMvc.perform(delete("/api/management/variants/90003"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("IT_VP_10 | PUT /copies/90001/price-override → 200 OK")
    void overrideCopyPrice() throws Exception {
        OverrideCopyPriceRequest req = new OverrideCopyPriceRequest();
        req.setSellPriceOverride(250000.0);

        mockMvc.perform(put("/api/management/variants/copies/90001/price-override")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_VP_11: GET /variants/{variantId}/copies ──
    @Test
    @DisplayName("IT_VP_11 | GET /variants/90001/copies → 200 + copy pricing list")
    void getCopiesByVariant() throws Exception {
        mockMvc.perform(get("/api/management/variants/90001/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_VP_12: PUT /variants/99999 → 404 ──
    @Test
    @DisplayName("IT_VP_12 | PUT /variants/99999 → 404 Not Found")
    void updateVariant_notFound() throws Exception {
        UpdateVariantRequest req = new UpdateVariantRequest();
        req.setEdition("Ghost");

        mockMvc.perform(put("/api/management/variants/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_VP_13: DELETE /variants/99999 → 404 ──
    @Test
    @DisplayName("IT_VP_13 | DELETE /variants/99999 → 404 Not Found")
    void deleteVariant_notFound() throws Exception {
        mockMvc.perform(delete("/api/management/variants/99999"))
                .andExpect(status().isNotFound());
    }

    // ── IT_VP_14: PUT /copies/99999/price-override → 404 ──
    @Test
    @DisplayName("IT_VP_14 | PUT /copies/99999/price-override → 404 Not Found")
    void overrideCopyPrice_notFound() throws Exception {
        OverrideCopyPriceRequest req = new OverrideCopyPriceRequest();
        req.setSellPriceOverride(100000.0);

        mockMvc.perform(put("/api/management/variants/copies/99999/price-override")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_VP_15: POST /variants (duplicate SKU) → 400 ──
    @Test
    @DisplayName("IT_VP_15 | POST /variants (duplicate SKU) → 400 Bad Request")
    void createVariant_duplicateSku() throws Exception {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setBookId(90001L);
        req.setSku("TEST-CC-HC"); // already exists
        req.setFormat("HARDCOVER");

        mockMvc.perform(post("/api/management/variants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── IT_VP_16: PUT price → salePrice > listPrice (Boundary) ──
    @Test
    @DisplayName("IT_VP_16 | PUT /variants/90001/price (salePrice > listPrice) → 400")
    void setBasePrice_salePriceGreaterThanList() throws Exception {
        SetBasePriceRequest req = new SetBasePriceRequest();
        req.setListPrice(100000.0);
        req.setSalePrice(200000.0); // sale > list

        mockMvc.perform(put("/api/management/variants/90001/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── IT_VP_17: PUT /variants/99999/price → 404 ──
    @Test
    @DisplayName("IT_VP_17 | PUT /variants/99999/price → 404 Not Found")
    void setBasePrice_notFound() throws Exception {
        SetBasePriceRequest req = new SetBasePriceRequest();
        req.setListPrice(100000.0);
        req.setSalePrice(90000.0);

        mockMvc.perform(put("/api/management/variants/99999/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_VP_18: PUT /copies/{id}/price-override (null → clear) ──
    @Test
    @DisplayName("IT_VP_18 | PUT /copies/90001/price-override (null → clear) → 200 OK")
    void overrideCopyPrice_clearOverride() throws Exception {
        OverrideCopyPriceRequest req = new OverrideCopyPriceRequest();
        req.setSellPriceOverride(null); // null = clear override

        mockMvc.perform(put("/api/management/variants/copies/90001/price-override")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_VP_19: GET /variants?bookId=99999 → empty list (Boundary) ──
    @Test
    @DisplayName("IT_VP_19 | GET /variants?bookId=99999 → 200 + empty list")
    void getVariantsByBook_noBook() throws Exception {
        mockMvc.perform(get("/api/management/variants").param("bookId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── IT_VP_20: GET /variants/90001 → field check ──
    @Test
    @DisplayName("IT_VP_20 | GET /variants/90001 → 200 + full detail fields")
    void getVariantById_fullDetailCheck() throws Exception {
        mockMvc.perform(get("/api/management/variants/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(90001))
                .andExpect(jsonPath("$.sku").value("TEST-CC-HC"))
                .andExpect(jsonPath("$.format").value("HARDCOVER"))
                .andExpect(jsonPath("$.edition").value("1st"))
                .andExpect(jsonPath("$.language").value("vi"));
    }
}
