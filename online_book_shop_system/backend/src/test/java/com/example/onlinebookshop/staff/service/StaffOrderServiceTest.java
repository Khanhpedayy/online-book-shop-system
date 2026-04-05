package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.payos.PayOsPaymentSyncService;
import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import com.example.onlinebookshop.staff.dto.StaffAlert;
import com.example.onlinebookshop.staff.repo.StaffAlertRepository;
import com.example.onlinebookshop.staff.repo.StaffOrderQueryRepository;
import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import com.example.onlinebookshop.staff.repo.StaffPackingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Order Service Tests")
class StaffOrderServiceTest {

    @Mock private StaffOrderRepository orderRepo;
    @Mock private StaffOrderQueryRepository queryRepo;
    @Mock private StaffAlertRepository alertRepo;
    @Mock private StaffPackingRepository packingRepo;
    @Mock private PayOsPaymentSyncService payOsPaymentSyncService;

    @InjectMocks private StaffOrderService service;

    @Test
    @DisplayName("UTC001_getAll_DefaultFilter_DelegatesToQueryRepository")
    void UTC001_getAll_DefaultFilter_DelegatesToQueryRepository() {
        var filter = new OrderFilter();
        when(queryRepo.findOrders(filter, 200)).thenReturn(List.of(new OrderListRow()));

        var rows = service.getAll(filter);

        assertEquals(1, rows.size());
        verify(queryRepo).findOrders(filter, 200);
    }

    @Test
    @DisplayName("UTC002_getById_OrderMissing_ThrowsRuntimeException")
    void UTC002_getById_OrderMissing_ThrowsRuntimeException() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(99L));
    }

    @Test
    @DisplayName("UTC003_updateStaffNote_TrimsAndSaves")
    void UTC003_updateStaffNote_TrimsAndSaves() {
        var order = new Order();
        order.setId(1L);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        service.updateStaffNote(1L, "  ghi chu noi bo  ");

        assertEquals("ghi chu noi bo", order.getStaffNote());
        verify(orderRepo).save(order);
    }

    @Test
    @DisplayName("UTC004_updateShipment_TrimsCarrierAndTracking")
    void UTC004_updateShipment_TrimsCarrierAndTracking() {
        var order = new Order();
        order.setId(2L);
        when(orderRepo.findById(2L)).thenReturn(Optional.of(order));

        service.updateShipment(2L, "  GHN ", "  TRK123456 ");

        assertEquals("GHN", order.getCarrier());
        assertEquals("TRK123456", order.getTrackingCode());
        verify(orderRepo).save(order);
    }

    @Test
    @DisplayName("UTC005_updatePaymentStatus_BlankValue_Throws")
    void UTC005_updatePaymentStatus_BlankValue_Throws() {
        var order = new Order();
        order.setId(3L);
        when(orderRepo.findById(3L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.updatePaymentStatus(3L, " "));
        verify(orderRepo, never()).save(any());
        verify(orderRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("UTC006_updateStatus_CONFIRMED_SetsTimestampAndStatus")
    void UTC006_updateStatus_CONFIRMED_SetsTimestampAndStatus() {
        var order = new Order();
        order.setId(4L);
        order.setStatus("NEW");
        when(orderRepo.findById(4L)).thenReturn(Optional.of(order));

        service.updateStatus(4L, "confirmed");

        assertEquals("CONFIRMED", order.getStatus());
        assertNotNull(order.getConfirmedAt());
        verify(orderRepo).save(order);
    }

    @Test
    @DisplayName("UTC007_updateStatus_FinalStateRejected")
    void UTC007_updateStatus_FinalStateRejected() {
        var order = new Order();
        order.setId(5L);
        order.setStatus("COMPLETED");
        when(orderRepo.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> service.updateStatus(5L, "CANCELLED"));
        verify(orderRepo, never()).save(any());
    }

    @Test
    @DisplayName("UTC008_getDashboardStats_WhenRepositoryFails_ReturnsZeros")
    void UTC008_getDashboardStats_WhenRepositoryFails_ReturnsZeros() {
        when(orderRepo.countNewOrders()).thenThrow(new RuntimeException("DB down"));

        var stats = service.getDashboardStats();

        assertEquals(0, stats.getNewOrders());
        assertEquals(0, stats.getPendingPayments());
        assertEquals(0, stats.getToPack());
        assertEquals(0, stats.getShippedToday());
        assertEquals(0, stats.getOverdue());
    }

    @Test
    @DisplayName("UTC009_getAlerts_DelegatesToAlertRepository")
    void UTC009_getAlerts_DelegatesToAlertRepository() {
        when(alertRepo.getAlerts(20)).thenReturn(List.of(new StaffAlert()));
        assertEquals(1, service.getAlerts().size());
        verify(alertRepo).getAlerts(20);
    }

    @Test
    @DisplayName("UTC010_getDetail_DelegatesToQueryRepository")
    void UTC010_getDetail_DelegatesToQueryRepository() {
        var view = new OrderDetailView();
        when(queryRepo.getOrderDetail(88L)).thenReturn(view);

        assertSame(view, service.getDetail(88L));
        verify(queryRepo).getOrderDetail(88L);
    }
}
