package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffDeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Delivery Controller Tests")
class StaffDeliveryControllerTest {

    @Mock private StaffDeliveryService service;
    @InjectMocks private StaffDeliveryController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC083_setOutcome_Success_RedirectsBack")
    void MVC083_setOutcome_Success_RedirectsBack() throws Exception {
        when(service.setDeliveryOutcome(100L, "DELIVERED", null))
                .thenReturn(new StaffDeliveryService.DeliveryOutcomeResult("ok"));

        mvc().perform(post("/staff/orders/100/delivery-outcome").param("outcome", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100/delivery"))
                .andExpect(flash().attributeExists("successMsg"));
    }
}
