package com.example.onlinebookshop.report;

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
@DisplayName("Report Controller Tests")
class ReportControllerTest {

    @Mock
    private ReportService service;

    @InjectMocks
    private ReportController controller;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /reports/sales/daily → 200")
    void getSalesByDay_returnsOk() throws Exception {
        SalesReportDTO dto = new SalesReportDTO();
        dto.setPeriod("2026-03-01");
        dto.setTotalOrders(15);
        dto.setTotalRevenue(1500.0);
        when(service.getSalesByDay()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/sales/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2026-03-01"))
                .andExpect(jsonPath("$[0].totalOrders").value(15));
    }

    @Test
    @DisplayName("GET /reports/sales/monthly → 200")
    void getSalesByMonth_returnsOk() throws Exception {
        SalesReportDTO dto = new SalesReportDTO();
        dto.setPeriod("2026-03");
        when(service.getSalesByMonth()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/sales/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2026-03"));
    }

    @Test
    @DisplayName("GET /reports/sales/top-selling?limit=10 → 200")
    void getTopSelling_returnsOk() throws Exception {
        TopSellingDTO dto = new TopSellingDTO();
        dto.setBookId(1L);
        dto.setTitle("Clean Code");
        dto.setTotalSold(200);
        when(service.getTopSelling(10)).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/sales/top-selling").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].totalSold").value(200));
    }

    @Test
    @DisplayName("GET /reports/sales/top-selling default limit=20 → 200")
    void getTopSelling_defaultLimit() throws Exception {
        when(service.getTopSelling(20)).thenReturn(List.of());

        mockMvc().perform(get("/api/reports/sales/top-selling"))
                .andExpect(status().isOk());
        verify(service).getTopSelling(20);
    }

    @Test
    @DisplayName("GET /reports/slow-movers → 200")
    void getSlowMovers_returnsOk() throws Exception {
        SlowMoverDTO dto = new SlowMoverDTO();
        dto.setSku("SKU-SLOW");
        dto.setDaysSinceLastSale(45L);
        when(service.getSlowMovers()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/slow-movers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].daysSinceLastSale").value(45));
    }

    @Test
    @DisplayName("GET /reports/lot-aging → 200")
    void getLotAging_returnsOk() throws Exception {
        LotAgingDTO dto = new LotAgingDTO();
        dto.setLotCode("LOT-OLD");
        dto.setAgeBucket("90+");
        dto.setAgeDays(120L);
        when(service.getLotAging()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/lot-aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ageBucket").value("90+"));
    }

    @Test
    @DisplayName("GET /reports/inventory-value → 200")
    void getInventoryValue_returnsOk() throws Exception {
        InventoryValueDTO dto = new InventoryValueDTO();
        dto.setSku("SKU-001");
        dto.setTotalCostValue(50000.0);
        when(service.getInventoryValue()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalCostValue").value(50000.0));
    }

    @Test
    @DisplayName("GET /reports/shrinkage → 200")
    void getShrinkage_returnsOk() throws Exception {
        ShrinkageDTO dto = new ShrinkageDTO();
        dto.setReason("DAMAGED");
        dto.setTotalQty(50);
        dto.setEstimatedLoss(2500.0);
        when(service.getShrinkage()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/reports/shrinkage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("DAMAGED"))
                .andExpect(jsonPath("$[0].estimatedLoss").value(2500.0));
    }

    @Test
    @DisplayName("GET /reports/summary → 200 dashboard")
    void getSummary_returnsOk() throws Exception {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalBooks(500);
        dto.setTotalVariants(800);
        dto.setTotalCopiesAvailable(5000);
        dto.setTotalInventoryValue(250000.0);
        dto.setTotalOrders(1200);
        dto.setTotalRevenue(180000.0);
        dto.setLowStockCount(15);
        dto.setOverstockCount(8);
        when(service.getDashboardSummary()).thenReturn(dto);

        mockMvc().perform(get("/api/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(500))
                .andExpect(jsonPath("$.totalCopiesAvailable").value(5000))
                .andExpect(jsonPath("$.lowStockCount").value(15));
    }
}
