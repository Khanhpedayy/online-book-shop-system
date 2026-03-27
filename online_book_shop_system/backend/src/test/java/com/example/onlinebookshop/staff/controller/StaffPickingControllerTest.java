package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.service.StaffPickingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Picking Controller Tests")
class StaffPickingControllerTest {

    @Mock private StaffPickingService picking;
    @InjectMocks private StaffPickingController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC040_autoAllocate_WithWarnings_RedirectsWithWarnFlash")
    void MVC040_autoAllocate_WithWarnings_RedirectsWithWarnFlash() throws Exception {
        when(picking.autoAllocate(100L)).thenReturn(new StaffPickingService.AllocationResult(1, List.of("warn1")));

        mvc().perform(post("/staff/orders/100/allocate-auto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/orders/100"))
                .andExpect(flash().attributeExists("warnMsg"));
    }

    @Test
    @DisplayName("MVC041_pickScan_Success_RedirectsWithSuccessFlash")
    void MVC041_pickScan_Success_RedirectsWithSuccessFlash() throws Exception {
        mvc().perform(post("/staff/orders/100/pick-scan")
                        .param("orderItemId", "10")
                        .param("copyCode", "COPY-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMsg"));

        verify(picking).pickByScan(100L, 10L, "COPY-01");
    }
}
