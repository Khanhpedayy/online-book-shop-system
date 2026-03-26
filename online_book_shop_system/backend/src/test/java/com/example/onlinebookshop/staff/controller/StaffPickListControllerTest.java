package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.dto.PickListView;
import com.example.onlinebookshop.staff.service.StaffPickListService;
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
@DisplayName("Staff Pick List Controller Tests")
class StaffPickListControllerTest {

    @Mock private StaffPickListService service;
    @InjectMocks private StaffPickListController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC060_pickList_ReturnsView")
    void MVC060_pickList_ReturnsView() throws Exception {
        when(service.getPickList(100L)).thenReturn(new PickListView());

        mvc().perform(get("/staff/orders/100/pick-list"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/pick-list"))
                .andExpect(model().attributeExists("v"));
    }

    @Test
    @DisplayName("MVC061_scan_Success_RedirectsBack")
    void MVC061_scan_Success_RedirectsBack() throws Exception {
        mvc().perform(post("/staff/orders/100/pick-list/scan").param("copyCode", "COPY-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100/pick-list"))
                .andExpect(flash().attributeExists("successMsg"));
    }
}
