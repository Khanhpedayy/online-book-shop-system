package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffDashboardStats;
import com.example.onlinebookshop.staff.service.StaffOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Dashboard Controller Tests")
class StaffDashboardControllerTest {

    @Mock private StaffOrderService service;
    @InjectMocks private StaffDashboardController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC001_dashboard_Authenticated_ReturnsDashboardView")
    void MVC001_dashboard_Authenticated_ReturnsDashboardView() throws Exception {
        when(service.getDashboardStats()).thenReturn(new StaffDashboardStats(1,2,3,4,5));
        when(service.getTodoList()).thenReturn(List.of());
        when(service.getAlerts()).thenReturn(List.of());

        mvc().perform(get("/staff/dashboard").principal(new TestingAuthenticationToken("staff01", "pw")))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/dashboard"))
                .andExpect(model().attributeExists("stats", "todoOrders", "alerts"))
                .andExpect(model().attribute("username", "staff01"));
    }
}
