package com.example.onlinebookshop.allocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Allocation Controller Tests")
class AllocationControllerTest {

    @Mock
    private AllocationService service;

    @InjectMocks
    private AllocationController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /settings/allocation → 200")
    void getSettings_returnsOk() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO();
        dto.setFifoBy("LOT");
        dto.setReservationTtlMin(30);
        dto.setConditionPriority("NEW>LIKE_NEW>GOOD>FAIR");
        dto.setAllowStaffOverride(true);
        when(service.getSettings()).thenReturn(dto);

        mockMvc().perform(get("/api/settings/allocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").value("LOT"))
                .andExpect(jsonPath("$.reservationTtlMin").value(30))
                .andExpect(jsonPath("$.allowStaffOverride").value(true));
    }

    @Test
    @DisplayName("PUT /settings/allocation → 200")
    void updateSettings_returnsOk() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO();
        dto.setFifoBy("COPY");
        dto.setReservationTtlMin(60);
        dto.setConditionPriority("NEW>GOOD");
        dto.setAllowStaffOverride(false);

        when(service.updateSettings(any())).thenReturn(dto);

        mockMvc().perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").value("COPY"))
                .andExpect(jsonPath("$.reservationTtlMin").value(60));
    }
}
