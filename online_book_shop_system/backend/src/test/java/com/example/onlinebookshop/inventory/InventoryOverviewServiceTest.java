package com.example.onlinebookshop.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Overview Service Tests")
class InventoryOverviewServiceTest {

    @Mock
    private InventoryOverviewRepository repository;

    @InjectMocks
    private InventoryOverviewService service;

    @Test
    @DisplayName("getStockByVariant delegates to repository")
    void getStockByVariant_callsRepo() {
        StockByVariantDTO dto = new StockByVariantDTO();
        dto.setVariantId(1L);
        when(repository.getStockByVariant()).thenReturn(List.of(dto));

        List<StockByVariantDTO> result = service.getStockByVariant();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getVariantId());
        verify(repository, times(1)).getStockByVariant();
    }

    @Test
    @DisplayName("getStockByLot delegates to repository")
    void getStockByLot_callsRepo() {
        when(repository.getStockByLot()).thenReturn(List.of());
        List<StockByLotDTO> result = service.getStockByLot();
        assertTrue(result.isEmpty());
        verify(repository).getStockByLot();
    }

    @Test
    @DisplayName("getStockByCondition delegates to repository")
    void getStockByCondition_callsRepo() {
        StockByConditionDTO dto = new StockByConditionDTO();
        dto.setCondition("NEW");
        when(repository.getStockByCondition()).thenReturn(List.of(dto));

        List<StockByConditionDTO> result = service.getStockByCondition();

        assertEquals(1, result.size());
        assertEquals("NEW", result.get(0).getCondition());
    }

    @Test
    @DisplayName("getLowStockAlerts uses threshold = 20")
    void getLowStockAlerts_usesThreshold20() {
        when(repository.getLowStockAlerts(20)).thenReturn(List.of());

        service.getLowStockAlerts();

        verify(repository).getLowStockAlerts(20);
        verify(repository, never()).getLowStockAlerts(10); // ensure it's not another value
    }

    @Test
    @DisplayName("getOverstockAgingAlerts uses threshold = 90 days")
    void getOverstockAlerts_usesThreshold90() {
        when(repository.getAgingLotAlerts(90)).thenReturn(List.of());

        service.getOverstockAgingAlerts();

        verify(repository).getAgingLotAlerts(90);
    }
}
