package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffShippingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Shipping Controller Tests")
class StaffShippingControllerTest {

    @Mock private StaffShippingService service;
    @InjectMocks private StaffShippingController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC080_shipScreen_ReturnsView")
    void MVC080_shipScreen_ReturnsView() throws Exception {
        when(service.getShippingView(100L)).thenReturn(new StaffShippingService.ShippingView());

        mvc().perform(get("/staff/orders/100/ship"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/ship"))
                .andExpect(model().attributeExists("v"));
    }

    @Test
    @DisplayName("MVC081_confirmShip_Success_RedirectsBack")
    void MVC081_confirmShip_Success_RedirectsBack() throws Exception {
        mvc().perform(post("/staff/orders/100/ship/confirm")
                        .param("carrier", "GHN")
                        .param("trackingCode", "TRK123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100/ship"))
                .andExpect(flash().attributeExists("successMsg"));
    }
}
