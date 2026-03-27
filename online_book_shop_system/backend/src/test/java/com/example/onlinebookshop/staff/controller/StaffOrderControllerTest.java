package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.OrderDetailView;
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
@DisplayName("Staff Order Controller Tests")
class StaffOrderControllerTest {

    @Mock private StaffOrderService service;
    @InjectMocks private StaffOrderController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC010_list_Default_ReturnsOrderListView")
    void MVC010_list_Default_ReturnsOrderListView() throws Exception {
        when(service.getAll(any())).thenReturn(List.of());

        mvc().perform(get("/staff/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/order-list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @DisplayName("MVC011_detail_ReturnsOrderDetailView")
    void MVC011_detail_ReturnsOrderDetailView() throws Exception {
        when(service.getDetail(10L)).thenReturn(new OrderDetailView());

        mvc().perform(get("/staff/orders/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/order-detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("MVC012_updateStatus_Success_RedirectsBackToDetail")
    void MVC012_updateStatus_Success_RedirectsBackToDetail() throws Exception {
        mvc().perform(post("/staff/orders/10/status").param("newStatus", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/10"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(service).updateStatus(10L, "CONFIRMED");
    }
}
