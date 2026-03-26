package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.PickListView;
import com.example.onlinebookshop.staff.repo.StaffPickListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Pick List Service Tests")
class StaffPickListServiceTest {

    @Mock private StaffPickListRepository repo;
    @InjectMocks private StaffPickListService service;

    @Test
    @DisplayName("UTC060_getPickList_BindsItemsAndCounters")
    void UTC060_getPickList_BindsItemsAndCounters() {
        var view = new PickListView();
        view.setOrderId(100L);
        when(repo.getOrderHeader(100L)).thenReturn(view);
        when(repo.getPickListItems(100L)).thenReturn(List.of());
        when(repo.countAllocated(100L)).thenReturn(2);
        when(repo.countPicked(100L)).thenReturn(1);

        var result = service.getPickList(100L);

        assertEquals(100L, result.getOrderId());
        assertEquals(2, result.getTotalAllocated());
        assertEquals(1, result.getTotalPicked());
    }

    @Test
    @DisplayName("UTC061_scanConfirmPicked_BlankCode_Throws")
    void UTC061_scanConfirmPicked_BlankCode_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scanConfirmPicked(100L, "  "));
    }

    @Test
    @DisplayName("UTC062_scanConfirmPicked_UnknownCopy_Throws")
    void UTC062_scanConfirmPicked_UnknownCopy_Throws() {
        when(repo.findCopyIdByCode("BAD")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.scanConfirmPicked(100L, "BAD"));
    }

    @Test
    @DisplayName("UTC063_scanConfirmPicked_Success_MarksOrderItemAndCopy")
    void UTC063_scanConfirmPicked_Success_MarksOrderItemAndCopy() {
        when(repo.findCopyIdByCode("COPY-01")).thenReturn(Optional.of(77L));
        when(repo.findOrderItemIdByOrderAndCopy(100L, 77L)).thenReturn(Optional.of(10L));

        assertDoesNotThrow(() -> service.scanConfirmPicked(100L, "COPY-01"));

        verify(repo).markOrderItemPicked(10L, "SCAN");
        verify(repo).markCopyPicked(77L);
    }
}
