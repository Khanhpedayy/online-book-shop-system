package com.example.onlinebookshop.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Overview Controller Tests")
class InventoryOverviewControllerTest {

    @Mock
    private InventoryOverviewService service;

    @InjectMocks
    private InventoryOverviewController controller;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    /* ── GET /api/inventory/overview/variants ── */

    @Test
    @DisplayName("GET /variants → 200 OK with list")
    void getStockByVariant_returnsOk() throws Exception {
        StockByVariantDTO dto = new StockByVariantDTO();
        dto.setVariantId(1L);
        dto.setSku("SKU-001");
        dto.setTitle("Clean Code");
        dto.setFormat("HARDCOVER");
        dto.setTotalAvailable(50);

        when(service.getStockByVariant()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/overview/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].variantId").value(1))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].totalAvailable").value(50));

        verify(service, times(1)).getStockByVariant();
    }

    @Test
    @DisplayName("GET /variants → 200 OK empty list when no stock")
    void getStockByVariant_emptyList() throws Exception {
        when(service.getStockByVariant()).thenReturn(List.of());

        mockMvc().perform(get("/api/inventory/overview/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    /* ── GET /api/inventory/overview/lots ── */

    @Test
    @DisplayName("GET /lots → 200 OK with lot data")
    void getStockByLot_returnsOk() throws Exception {
        StockByLotDTO dto = new StockByLotDTO();
        dto.setLotId(10L);
        dto.setLotCode("LOT-2024-001");

        when(service.getStockByLot()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/overview/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lotId").value(10))
                .andExpect(jsonPath("$[0].lotCode").value("LOT-2024-001"));
    }

    /* ── GET /api/inventory/overview/conditions ── */

    @Test
    @DisplayName("GET /conditions → 200 OK with condition breakdown")
    void getStockByCondition_returnsOk() throws Exception {
        StockByConditionDTO dto = new StockByConditionDTO();
        dto.setCondition("NEW");
        dto.setTotalAvailable(100);

        when(service.getStockByCondition()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/overview/conditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].condition").value("NEW"))
                .andExpect(jsonPath("$[0].totalAvailable").value(100));
    }

    /* ── GET /api/inventory/overview/alerts/low-stock ── */

    @Test
    @DisplayName("GET /alerts/low-stock → 200 OK with alerts")
    void getLowStockAlerts_returnsOk() throws Exception {
        LowStockAlertDTO dto = new LowStockAlertDTO();
        dto.setVariantId(5L);
        dto.setSku("SKU-LOW");
        dto.setTotalAvailable(3);

        when(service.getLowStockAlerts()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/overview/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAvailable").value(3));
    }

    /* ── GET /api/inventory/overview/alerts/overstock ── */

    @Test
    @DisplayName("GET /alerts/overstock → 200 OK with aging alerts")
    void getOverstockAlerts_returnsOk() throws Exception {
        OverstockAlertDTO dto = new OverstockAlertDTO();
        dto.setLotId(20L);
        dto.setAgeDays(120L);

        when(service.getOverstockAgingAlerts()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/overview/alerts/overstock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ageDays").value(120));
    }
}
