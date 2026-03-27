package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffPackingRepository;
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
@DisplayName("Staff Packing Service Tests")
class StaffPackingServiceTest {

    @Mock private StaffPackingRepository repo;
    @Mock private StaffShippingRepository shippingRepo;
    @InjectMocks private StaffPackingService service;

    @Test
    @DisplayName("UTC070_getPackingView_LoadsHeaderCountsAndItems")
    void UTC070_getPackingView_LoadsHeaderCountsAndItems() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffPackingRepository.OrderHeader(
                100L, "ORD-100", "CONFIRMED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, null, null, null));
        when(repo.countPackableItems(100L)).thenReturn(2);
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(1);
        when(shippingRepo.getItemsForSlip(100L)).thenReturn(List.of());

        var view = service.getPackingView(100L);

        assertEquals("ORD-100", view.orderCode);
        assertEquals(2, view.packableItems);
        assertEquals(2, view.allocatedItems);
        assertEquals(1, view.pickedItems);
    }

    @Test
    @DisplayName("UTC071_confirmPacked_InvalidBoxCount_Throws")
    void UTC071_confirmPacked_InvalidBoxCount_Throws() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmPacked(100L, 0, null));
    }

    @Test
    @DisplayName("UTC072_confirmPacked_NotEnoughPicked_Throws")
    void UTC072_confirmPacked_NotEnoughPicked_Throws() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffPackingRepository.OrderHeader(
                100L, "ORD-100", "CONFIRMED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, null, null, null));
        when(repo.countPackableItems(100L)).thenReturn(2);
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(1);

        assertThrows(IllegalStateException.class, () -> service.confirmPacked(100L, 1, "note"));
    }

    @Test
    @DisplayName("UTC073_confirmPacked_Success_CallsMarkPacked")
    void UTC073_confirmPacked_Success_CallsMarkPacked() {
        when(repo.getOrderHeader(100L)).thenReturn(new StaffPackingRepository.OrderHeader(
                100L, "ORD-100", "CONFIRMED", "PAID", "A", "0909", "L1", null,
                null, null, null, null, null, null, null));
        when(repo.countPackableItems(100L)).thenReturn(2);
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(2);
        when(repo.markPacked(100L, 2, "done")).thenReturn(1);

        assertDoesNotThrow(() -> service.confirmPacked(100L, 2, "done"));
        verify(repo).markPacked(100L, 2, "done");
    }
}
