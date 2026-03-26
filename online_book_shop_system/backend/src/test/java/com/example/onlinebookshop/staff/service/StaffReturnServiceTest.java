package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.OrderItemRow;
import com.example.onlinebookshop.staff.dto.ReturnIntakeView;
import com.example.onlinebookshop.staff.repo.StaffOrderQueryRepository;
import com.example.onlinebookshop.staff.repo.StaffReturnRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Return Service Tests")
class StaffReturnServiceTest {

    @Mock private StaffReturnRepository repo;
    @Mock private StaffOrderQueryRepository orderQuery;
    @InjectMocks private StaffReturnService service;

    @Test
    @DisplayName("UTC110_buildCreateScreen_LoadsOrderInfoAndOrderItems")
    void UTC110_buildCreateScreen_LoadsOrderInfoAndOrderItems() {
        var view = new ReturnIntakeView();
        view.setOrderId(100L);
        when(repo.getCreateScreenOrderInfo(100L)).thenReturn(view);
        when(orderQuery.getOrderItems(100L)).thenReturn(List.of(new OrderItemRow()));

        var rs = service.buildCreateScreen(100L);

        assertEquals(1, rs.getOrderItems().size());
    }

    @Test
    @DisplayName("UTC111_createReturn_DelegatesWithTrimmedReasonAndNote")
    void UTC111_createReturn_DelegatesWithTrimmedReasonAndNote() {
        when(repo.insertReturn(eq(100L), anyString(), eq("reason"), eq("note"))).thenReturn(9001L);

        long id = service.createReturn(100L, " reason ", " note ");

        assertEquals(9001L, id);
        verify(repo).insertReturn(eq(100L), startsWith("RET-100-"), eq("reason"), eq("note"));
    }

    @Test
    @DisplayName("UTC112_scanReturnedCopy_BlankCode_Throws")
    void UTC112_scanReturnedCopy_BlankCode_Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scanReturnedCopy(1L, 10L, " ", "RESELLABLE", null));
    }

    @Test
    @DisplayName("UTC113_scanReturnedCopy_UnknownCopy_Throws")
    void UTC113_scanReturnedCopy_UnknownCopy_Throws() {
        when(repo.findCopyIdByCode("BAD")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.scanReturnedCopy(1L, 10L, "BAD", "RESELLABLE", null));
    }

    @Test
    @DisplayName("UTC114_scanReturnedCopy_AllocatedCopyMismatch_Throws")
    void UTC114_scanReturnedCopy_AllocatedCopyMismatch_Throws() {
        when(repo.findCopyIdByCode("COPY-01")).thenReturn(Optional.of(77L));
        when(repo.getOrderItemForReturn(1L, 10L))
                .thenReturn(new StaffReturnRepository.OrderItemForReturn(10L, 100L, 501L, 99L, "SKU", "TITLE"));

        assertThrows(IllegalArgumentException.class,
                () -> service.scanReturnedCopy(1L, 10L, "COPY-01", "RESELLABLE", null));
    }

    @Test
    @DisplayName("UTC115_scanReturnedCopy_Success_InsertsReturnItem")
    void UTC115_scanReturnedCopy_Success_InsertsReturnItem() {
        when(repo.findCopyIdByCode("COPY-01")).thenReturn(Optional.of(77L));
        when(repo.getOrderItemForReturn(1L, 10L))
                .thenReturn(new StaffReturnRepository.OrderItemForReturn(10L, 100L, 501L, 77L, "SKU", "TITLE"));
        when(repo.existsReturnItemByCopy(1L, 77L)).thenReturn(false);

        assertDoesNotThrow(() -> service.scanReturnedCopy(1L, 10L, "COPY-01", "resellable", " ok "));
        verify(repo).insertReturnItem(1L, 10L, 77L, "RESELLABLE", "ok");
    }
}
