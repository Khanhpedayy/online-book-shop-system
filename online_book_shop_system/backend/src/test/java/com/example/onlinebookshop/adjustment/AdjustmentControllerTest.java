package com.example.onlinebookshop.adjustment;

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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Adjustment Controller Tests")
class AdjustmentControllerTest {

    @Mock
    private AdjustmentService service;

    @InjectMocks
    private AdjustmentController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /adjustments → 200 list")
    void getAll_returnsOk() throws Exception {
        AdjustmentDTO dto = new AdjustmentDTO();
        dto.setId(1L);
        dto.setType("ADJUST");
        dto.setReason("DAMAGED");
        when(service.getAll()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("DAMAGED"));
    }

    @Test
    @DisplayName("GET /adjustments → 200 empty list")
    void getAll_emptyList() throws Exception {
        when(service.getAll()).thenReturn(List.of());

        mockMvc().perform(get("/api/inventory/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /adjustments → 201 Created")
    void create_returnsCreated() throws Exception {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setVariantId(1L);
        req.setLotId(1L);
        req.setQuantity(-5);
        req.setReason("DAMAGED");

        when(service.createAdjustment(any())).thenReturn(100L);

        mockMvc().perform(post("/api/inventory/adjustments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }
}
