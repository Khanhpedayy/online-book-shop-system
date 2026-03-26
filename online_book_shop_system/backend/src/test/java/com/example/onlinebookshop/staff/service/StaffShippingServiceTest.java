package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffShippingRepository;
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
@DisplayName("Staff Shipping Service Tests")
class StaffShippingServiceTest {

    @Mock private StaffShippingRepository repo;
    @InjectMocks private StaffShippingService service;

    @Test
    @DisplayName("UTC080_getShippingView_LoadsHeaderCountsAndItems")
    void UTC080_getShippingView_LoadsHeaderCountsAndItems() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffShippingRepository.OrderHeader(
                100L, "ORD-100", "PACKED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, "GHN", "TRK123456"));
        when(repo.countPackableItems(100L)).thenReturn(2);
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(2);
        when(repo.getItemsForSlip(100L)).thenReturn(List.of());

        var view = service.getShippingView(100L);

        assertEquals("ORD-100", view.orderCode);
        assertEquals("PACKED", view.status);
        assertEquals(2, view.totalItems);
    }

    @Test
    @DisplayName("UTC081_confirmShipped_BlankCarrier_Throws")
    void UTC081_confirmShipped_BlankCarrier_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.confirmShipped(100L, " ", "TRK123456"));
    }

    @Test
    @DisplayName("UTC082_confirmShipped_StatusNotPacked_Throws")
    void UTC082_confirmShipped_StatusNotPacked_Throws() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffShippingRepository.OrderHeader(
                100L, "ORD-100", "CONFIRMED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, null, null));

        assertThrows(IllegalStateException.class,
                () -> service.confirmShipped(100L, "GHN", "TRK123456"));
    }

    @Test
    @DisplayName("UTC083_confirmShipped_Success_CallsMarkShipped")
    void UTC083_confirmShipped_Success_CallsMarkShipped() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffShippingRepository.OrderHeader(
                100L, "ORD-100", "PACKED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, null, null));
        when(repo.countPackableItems(100L)).thenReturn(2);
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(2);
        when(repo.markShipped(100L, "GHN", "TRK123456")).thenReturn(1);

        assertDoesNotThrow(() -> service.confirmShipped(100L, "GHN", "TRK123456"));
        verify(repo).markShipped(100L, "GHN", "TRK123456");
    }
}
