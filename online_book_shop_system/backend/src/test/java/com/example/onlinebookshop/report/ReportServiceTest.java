package com.example.onlinebookshop.report;

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
@DisplayName("Report Service Tests")
class ReportServiceTest {

    @Mock
    private ReportRepository repo;

    @InjectMocks
    private ReportService service;

    @Test
    @DisplayName("getSalesByDay delegates to repo")
    void getSalesByDay() {
        when(repo.getSalesByDay()).thenReturn(List.of(new SalesReportDTO()));
        assertEquals(1, service.getSalesByDay().size());
    }

    @Test
    @DisplayName("getSalesByMonth delegates to repo")
    void getSalesByMonth() {
        when(repo.getSalesByMonth()).thenReturn(List.of());
        assertTrue(service.getSalesByMonth().isEmpty());
    }

    @Test
    @DisplayName("getTopSelling passes limit to repo")
    void getTopSelling() {
        when(repo.getTopSelling(10)).thenReturn(List.of());
        service.getTopSelling(10);
        verify(repo).getTopSelling(10);
    }

    @Test
    @DisplayName("getSlowMovers delegates to repo")
    void getSlowMovers() {
        when(repo.getSlowMovers()).thenReturn(List.of());
        assertTrue(service.getSlowMovers().isEmpty());
    }

    @Test
    @DisplayName("getLotAging delegates to repo")
    void getLotAging() {
        when(repo.getLotAging()).thenReturn(List.of());
        assertTrue(service.getLotAging().isEmpty());
    }

    @Test
    @DisplayName("getInventoryValue delegates to repo")
    void getInventoryValue() {
        when(repo.getInventoryValue()).thenReturn(List.of());
        assertTrue(service.getInventoryValue().isEmpty());
    }

    @Test
    @DisplayName("getShrinkage delegates to repo")
    void getShrinkage() {
        when(repo.getShrinkage()).thenReturn(List.of());
        assertTrue(service.getShrinkage().isEmpty());
    }

    @Test
    @DisplayName("getDashboardSummary returns complete data")
    void getDashboardSummary() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalBooks(500);
        dto.setTotalRevenue(180000.0);
        when(repo.getDashboardSummary()).thenReturn(dto);

        DashboardSummaryDTO result = service.getDashboardSummary();
        assertEquals(500, result.getTotalBooks());
        assertEquals(180000.0, result.getTotalRevenue());
    }
}
