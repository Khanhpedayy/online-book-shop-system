package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Delivery Service Tests")
class StaffDeliveryServiceTest {

    @Mock private StaffOrderRepository orderRepo;
    @InjectMocks private StaffDeliveryService service;

    @Test
    @DisplayName("UTC088_setDeliveryOutcome_DELIVERED_OnPaidOrder_AutoCompletes")
    void UTC088_setDeliveryOutcome_DELIVERED_OnPaidOrder_AutoCompletes() {
        var order = new Order();
        order.setId(100L);
        order.setStatus("SHIPPED");
        order.setPaymentStatus("PAID");
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));

        var rs = service.setDeliveryOutcome(100L, "DELIVERED", null);

        assertEquals("COMPLETED", order.getStatus());
        assertNotNull(order.getDeliveredAt());
        assertNotNull(order.getCompletedAt());
        assertTrue(rs.message().contains("COMPLETED"));
        verify(orderRepo).save(order);
    }

    @Test
    @DisplayName("UTC089_setDeliveryOutcome_FAILED_RequiresReason")
    void UTC089_setDeliveryOutcome_FAILED_RequiresReason() {
        var order = new Order();
        order.setId(100L);
        order.setStatus("SHIPPED");
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> service.setDeliveryOutcome(100L, "FAILED", " "));
    }

    @Test
    @DisplayName("UTC090_setDeliveryOutcome_FAILED_AppendsStaffNote")
    void UTC090_setDeliveryOutcome_FAILED_AppendsStaffNote() {
        var order = new Order();
        order.setId(100L);
        order.setStatus("SHIPPED");
        order.setStaffNote("old note");
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));

        var rs = service.setDeliveryOutcome(100L, "FAILED", "customer not home");

        assertTrue(order.getStaffNote().contains("DELIVERY FAILED"));
        assertTrue(rs.message().contains("giao thất bại"));
        verify(orderRepo).save(order);
    }
}
