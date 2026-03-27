package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.ReturnIntakeView;
import com.example.onlinebookshop.staff.service.StaffReturnService;
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
@DisplayName("Staff Return Controller Tests")
class StaffReturnControllerTest {

    @Mock private StaffReturnService service;
    @InjectMocks private StaffReturnController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC110_createScreen_ReturnsView")
    void MVC110_createScreen_ReturnsView() throws Exception {
        when(service.buildCreateScreen(100L)).thenReturn(new ReturnIntakeView());

        mvc().perform(get("/staff/returns/new").param("orderId", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/return-create"))
                .andExpect(model().attributeExists("v"));
    }

    @Test
    @DisplayName("MVC111_create_Success_RedirectsToIntake")
    void MVC111_create_Success_RedirectsToIntake() throws Exception {
        when(service.createReturn(100L, "reason", "note")).thenReturn(9001L);

        mvc().perform(post("/staff/returns/create")
                        .param("orderId", "100")
                        .param("reason", "reason")
                        .param("note", "note"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/returns/9001"))
                .andExpect(flash().attributeExists("successMsg"));
    }

    @Test
    @DisplayName("MVC114_scan_Success_RedirectsBack")
    void MVC114_scan_Success_RedirectsBack() throws Exception {
        mvc().perform(post("/staff/returns/1/scan")
                        .param("orderItemId", "10")
                        .param("copyCode", "COPY-01")
                        .param("conditionGrade", "RESELLABLE")
                        .param("conditionNote", "ok"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/returns/1"))
                .andExpect(flash().attributeExists("successMsg"));
    }
}
