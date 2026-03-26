package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPackingService;
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
@DisplayName("Staff Packing Controller Tests")
class StaffPackingControllerTest {

    @Mock private StaffPackingService service;
    @InjectMocks private StaffPackingController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC070_packScreen_ReturnsView")
    void MVC070_packScreen_ReturnsView() throws Exception {
        when(service.getPackingView(100L)).thenReturn(new StaffPackingService.PackingView());

        mvc().perform(get("/staff/orders/100/pack"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/pack"))
                .andExpect(model().attributeExists("v"));
    }

    @Test
    @DisplayName("MVC071_confirmPack_Success_RedirectsBack")
    void MVC071_confirmPack_Success_RedirectsBack() throws Exception {
        mvc().perform(post("/staff/orders/100/pack/confirm")
                        .param("boxCount", "2")
                        .param("packingNote", "done"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100/pack"))
                .andExpect(flash().attributeExists("successMsg"));
    }
}
