package com.example.onlinebookshop.variantpricing;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Variant & Pricing Controller Tests")
class VariantPricingControllerTest {

    @Mock
    private VariantPricingService service;

    @InjectMocks
    private VariantPricingController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    /* ── GET /api/management/variants ── */

    @Test
    @DisplayName("GET /variants → 200 all variants")
    void getAllVariants_noFilter() throws Exception {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        dto.setSku("SKU-001");
        when(service.getAllVariants()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/management/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-001"));
    }

    @Test
    @DisplayName("GET /variants?bookId=5 → 200 filtered")
    void getAllVariants_byBookId() throws Exception {
        VariantDTO dto = new VariantDTO();
        dto.setId(2L);
        dto.setBookId(5L);
        when(service.getVariantsByBook(5L)).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/management/variants").param("bookId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(5));
    }

    /* ── GET /api/management/variants/{id} ── */

    @Test
    @DisplayName("GET /variants/1 → 200 when found")
    void getVariant_found() throws Exception {
        VariantDTO dto = new VariantDTO();
        dto.setId(1L);
        dto.setFormat("HARDCOVER");
        when(service.getVariantById(1L)).thenReturn(dto);

        mockMvc().perform(get("/api/management/variants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("HARDCOVER"));
    }

    @Test
    @DisplayName("GET /variants/999 → 404 when not found")
    void getVariant_notFound() throws Exception {
        when(service.getVariantById(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/management/variants/999"))
                .andExpect(status().isNotFound());
    }

    /* ── POST /api/management/variants ── */

    @Test
    @DisplayName("POST /variants → 201 Created")
    void createVariant_valid() throws Exception {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setBookId(1L);
        req.setSku("SKU-NEW");
        req.setFormat("PAPERBACK");

        VariantDTO created = new VariantDTO();
        created.setId(10L);
        created.setSku("SKU-NEW");
        when(service.createVariant(any())).thenReturn(created);

        mockMvc().perform(post("/api/management/variants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-NEW"));
    }

    @Test
    @DisplayName("POST /variants → 400 invalid (missing bookId)")
    void createVariant_invalid() throws Exception {
        when(service.createVariant(any())).thenThrow(new IllegalArgumentException("bookId is required"));

        mockMvc().perform(post("/api/management/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /* ── PUT /api/management/variants/{id} ── */

    @Test
    @DisplayName("PUT /variants/1 → 200 OK")
    void updateVariant_found() throws Exception {
        VariantDTO updated = new VariantDTO();
        updated.setId(1L);
        when(service.updateVariant(eq(1L), any())).thenReturn(updated);

        mockMvc().perform(put("/api/management/variants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /variants/999 → 404")
    void updateVariant_notFound() throws Exception {
        when(service.updateVariant(eq(999L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(put("/api/management/variants/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    /* ── DELETE /api/management/variants/{id} ── */

    @Test
    @DisplayName("DELETE /variants/1 → 204")
    void deleteVariant_found() throws Exception {
        doNothing().when(service).deleteVariant(1L);

        mockMvc().perform(delete("/api/management/variants/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /variants/999 → 404")
    void deleteVariant_notFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(service).deleteVariant(999L);

        mockMvc().perform(delete("/api/management/variants/999"))
                .andExpect(status().isNotFound());
    }

    /* ── PUT /api/management/variants/{id}/price ── */

    @Test
    @DisplayName("PUT /variants/1/price → 200 valid prices")
    void setBasePrice_valid() throws Exception {
        SetBasePriceRequest req = new SetBasePriceRequest();
        req.setListPrice(29.99);
        req.setSalePrice(24.99);

        VariantDTO result = new VariantDTO();
        result.setId(1L);
        result.setListPrice(29.99);
        when(service.setBasePrice(1L, 29.99, 24.99)).thenReturn(result);

        mockMvc().perform(put("/api/management/variants/1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listPrice").value(29.99));
    }

    @Test
    @DisplayName("PUT /variants/1/price → 400 negative price")
    void setBasePrice_invalid() throws Exception {
        SetBasePriceRequest req = new SetBasePriceRequest();
        req.setListPrice(-1.0);
        req.setSalePrice(0.0);

        when(service.setBasePrice(1L, -1.0, 0.0))
                .thenThrow(new IllegalArgumentException("listPrice must be >= 0"));

        mockMvc().perform(put("/api/management/variants/1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    /* ── PUT /api/management/variants/{id}/condition-prices ── */

    @Test
    @DisplayName("PUT /variants/1/condition-prices → 200")
    void setConditionPrices_valid() throws Exception {
        SetConditionPricesRequest req = new SetConditionPricesRequest();
        req.setConditionPricesJson("{\"LIKE_NEW\":{\"pct\":10}}");

        VariantDTO result = new VariantDTO();
        result.setId(1L);
        when(service.setConditionPrices(eq(1L), any())).thenReturn(result);

        mockMvc().perform(put("/api/management/variants/1/condition-prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /* ── GET /api/management/variants/{variantId}/copies ── */

    @Test
    @DisplayName("GET /variants/1/copies → 200 list")
    void getCopiesByVariant_returnsOk() throws Exception {
        CopyPricingDTO dto = new CopyPricingDTO();
        dto.setId(100L);
        dto.setCopyCode("CP-001");
        when(service.getCopiesByVariant(1L)).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/management/variants/1/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].copyCode").value("CP-001"));
    }

    /* ── PUT /api/management/variants/copies/{copyId}/price-override ── */

    @Test
    @DisplayName("PUT /copies/100/price-override → 200")
    void overrideCopyPrice_found() throws Exception {
        OverrideCopyPriceRequest req = new OverrideCopyPriceRequest();
        req.setSellPriceOverride(49.99);
        doNothing().when(service).overrideCopyPrice(100L, 49.99);

        mockMvc().perform(put("/api/management/variants/copies/100/price-override")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /copies/999/price-override → 404")
    void overrideCopyPrice_notFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(service).overrideCopyPrice(eq(999L), any());

        mockMvc().perform(put("/api/management/variants/copies/999/price-override")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sellPriceOverride\":10}"))
                .andExpect(status().isNotFound());
    }
}
