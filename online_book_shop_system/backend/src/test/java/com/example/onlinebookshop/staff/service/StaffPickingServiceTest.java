package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffPickingRepository;
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
@DisplayName("Staff Picking Service Tests")
class StaffPickingServiceTest {

    @Mock private StaffPickingRepository repo;
    @InjectMocks private StaffPickingService service;

    @Test
    @DisplayName("UTC040_autoAllocate_FIFOAvailableCopy_AssignsSuccessfully")
    void UTC040_autoAllocate_FIFOAvailableCopy_AssignsSuccessfully() {
        when(repo.findUnallocatedOrderItemIds(100L)).thenReturn(List.of(11L));
        when(repo.getVariantIdByOrderItemId(11L)).thenReturn(501L);
        when(repo.findFifoAvailableCopyId(501L)).thenReturn(Optional.of(9001L));
        when(repo.reserveCopy(9001L, 30)).thenReturn(1);
        when(repo.assignCopyToOrderItem(11L, 9001L, "AUTO")).thenReturn(1);

        var rs = service.autoAllocate(100L);

        assertEquals(1, rs.allocatedCount());
        assertTrue(rs.warnings().isEmpty());
        verify(repo).insertInventoryTxReserve(501L, 9001L, 100L, "AUTO allocate by staff");
    }

    @Test
    @DisplayName("UTC041_autoAllocate_NoStock_ReturnsWarning")
    void UTC041_autoAllocate_NoStock_ReturnsWarning() {
        when(repo.findUnallocatedOrderItemIds(100L)).thenReturn(List.of(11L));
        when(repo.getVariantIdByOrderItemId(11L)).thenReturn(501L);
        when(repo.findFifoAvailableCopyId(501L)).thenReturn(Optional.empty());

        var rs = service.autoAllocate(100L);

        assertEquals(0, rs.allocatedCount());
        assertEquals(1, rs.warnings().size());
    }

    @Test
    @DisplayName("UTC042_pickByScan_UnknownCopyCode_Throws")
    void UTC042_pickByScan_UnknownCopyCode_Throws() {
        when(repo.getVariantIdByOrderItemId(10L)).thenReturn(501L);
        when(repo.findCopyByCode("NO-SUCH")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.pickByScan(100L, 10L, "NO-SUCH"));
    }

    @Test
    @DisplayName("UTC043_pickByScan_VariantMismatch_Throws")
    void UTC043_pickByScan_VariantMismatch_Throws() {
        when(repo.getVariantIdByOrderItemId(10L)).thenReturn(501L);
        when(repo.findCopyByCode("COPY-1"))
                .thenReturn(Optional.of(new StaffPickingRepository.CopyInfo(77L, 999L, "AVAILABLE")));

        assertThrows(IllegalArgumentException.class,
                () -> service.pickByScan(100L, 10L, "COPY-1"));
    }

    @Test
    @DisplayName("UTC044_pickByScan_Success_ReservesAndAssigns")
    void UTC044_pickByScan_Success_ReservesAndAssigns() {
        when(repo.getVariantIdByOrderItemId(10L)).thenReturn(501L);
        when(repo.findCopyByCode("COPY-OK"))
                .thenReturn(Optional.of(new StaffPickingRepository.CopyInfo(77L, 501L, "AVAILABLE")));
        when(repo.reserveCopy(77L, 30)).thenReturn(1);
        when(repo.assignCopyToOrderItem(10L, 77L, "SCAN")).thenReturn(1);

        assertDoesNotThrow(() -> service.pickByScan(100L, 10L, "COPY-OK"));
        verify(repo).insertInventoryTxReserve(501L, 77L, 100L, "MANUAL scan by staff");
    }

    @Test
    @DisplayName("UTC045_unpick_WhenAssigned_ReleasesCopy")
    void UTC045_unpick_WhenAssigned_ReleasesCopy() {
        when(repo.getAssignedCopyId(10L)).thenReturn(Optional.of(77L));

        service.unpick(10L);

        verify(repo).unassignCopyFromOrderItem(10L);
        verify(repo).releaseCopy(77L);
    }
}
