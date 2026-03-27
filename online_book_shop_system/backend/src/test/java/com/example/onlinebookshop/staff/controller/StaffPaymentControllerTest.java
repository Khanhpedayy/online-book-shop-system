package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.StaffPaymentQueryRepository;
import com.example.onlinebookshop.staff.service.StaffOrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Payment Controller Tests")
class StaffPaymentControllerTest {

    @Mock private StaffPaymentQueryRepository paymentRepo;
    @Mock private StaffOrderService orderService;
    @InjectMocks private StaffPaymentController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC100_list_ReturnsPaymentLogsView")
    void MVC100_list_ReturnsPaymentLogsView() throws Exception {
        when(paymentRepo.listPayments(null, null, 200)).thenReturn(List.of());

        mvc().perform(get("/staff/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/payment-logs"))
                .andExpect(model().attributeExists("rows"));
    }

    @Test
    @DisplayName("MVC102_recheck_SucceededPayment_SyncsOrderAndRedirects")
    void MVC102_recheck_SucceededPayment_SyncsOrderAndRedirects() throws Exception {
        when(paymentRepo.getOrderIdByPaymentId(5L)).thenReturn(100L);
        when(paymentRepo.getPaymentStatus(5L)).thenReturn("SUCCEEDED");

        mvc().perform(post("/staff/payments/5/recheck"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(orderService).updatePaymentStatus(100L, "PAID");
    }
}
