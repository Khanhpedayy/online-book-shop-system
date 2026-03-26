package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.staff.repo.StaffCustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Staff Customer Controller Tests")
class StaffCustomerControllerTest {

    @Mock private StaffCustomerRepository repo;
    @InjectMocks private StaffCustomerController controller;

    private MockMvc mvc() { return MockMvcBuilders.standaloneSetup(controller).build(); }

    @Test
    @DisplayName("MVC130_lookup_ReturnsCustomerLookupView")
    void MVC130_lookup_ReturnsCustomerLookupView() throws Exception {
        when(repo.search("An", 200)).thenReturn(List.of());

        mvc().perform(get("/staff/customers").param("q", "An"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/customer-lookup"))
                .andExpect(model().attributeExists("q", "rows"));
    }
}
