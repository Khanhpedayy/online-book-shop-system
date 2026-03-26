package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Incident Controller Tests")
class StaffIncidentControllerTest {

    @Mock private AuditLogRepository audit;
    @InjectMocks private StaffIncidentController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC140_form_ReturnsIncidentFormView")
    void MVC140_form_ReturnsIncidentFormView() throws Exception {
        mvc().perform(get("/staff/incidents"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/incident-form"));
    }

    @Test
    @DisplayName("MVC141_submit_Success_RedirectsWithFlash")
    void MVC141_submit_Success_RedirectsWithFlash() throws Exception {
        mvc().perform(post("/staff/incidents")
                        .principal(new TestingAuthenticationToken("staff01", "pw"))
                        .param("entityTable", "orders")
                        .param("entityId", "100")
                        .param("note", "picked wrong copy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/incidents"))
                .andExpect(flash().attributeExists("successMsg"));

        verify(audit).log(null, "INCIDENT_REPORT", "orders", 100L, null, "picked wrong copy");
    }
}
