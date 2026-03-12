package com.example.onlinebookshop.stocktaking;

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
@DisplayName("Stocktaking Controller Tests")
class StocktakingControllerTest {

    @Mock
    private StocktakingService service;

    @InjectMocks
    private StocktakingController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /stocktaking → 200 list sessions")
    void getSessions_returnsOk() throws Exception {
        StocktakingSessionDTO dto = new StocktakingSessionDTO();
        dto.setSessionCode("ST-001");
        dto.setStatus("OPEN");
        when(service.getAllSessions()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/stocktaking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionCode").value("ST-001"));
    }

    @Test
    @DisplayName("GET /stocktaking/ST-001 → 200 detail")
    void getSession_found() throws Exception {
        StocktakingSessionDTO dto = new StocktakingSessionDTO();
        dto.setSessionCode("ST-001");
        when(service.getSession("ST-001")).thenReturn(dto);

        mockMvc().perform(get("/api/inventory/stocktaking/ST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionCode").value("ST-001"));
    }

    @Test
    @DisplayName("GET /stocktaking/INVALID → 404")
    void getSession_notFound() throws Exception {
        when(service.getSession("INVALID")).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/inventory/stocktaking/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /stocktaking → 201 Created")
    void createSession_returnsCreated() throws Exception {
        CreateStocktakingRequest req = new CreateStocktakingRequest();
        req.setScope("ALL");

        StocktakingSessionDTO created = new StocktakingSessionDTO();
        created.setSessionCode("ST-12345678");
        when(service.createSession(any())).thenReturn(created);

        mockMvc().perform(post("/api/inventory/stocktaking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionCode").value("ST-12345678"));
    }

    @Test
    @DisplayName("POST /stocktaking/ST-001/count → 200")
    void recordCount_success() throws Exception {
        RecordCountRequest req = new RecordCountRequest();
        req.setVariantId(1L);
        req.setCountedQty(45);

        StocktakingSessionDTO result = new StocktakingSessionDTO();
        result.setSessionCode("ST-001");
        when(service.recordCount(eq("ST-001"), any())).thenReturn(result);

        mockMvc().perform(post("/api/inventory/stocktaking/ST-001/count")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /stocktaking/ST-001/count → 400 error")
    void recordCount_error() throws Exception {
        when(service.recordCount(eq("ST-001"), any()))
                .thenThrow(new RuntimeException("Session already completed"));

        mockMvc().perform(post("/api/inventory/stocktaking/ST-001/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":1,\"countedQty\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /stocktaking/ST-001/apply → 200")
    void applyAdjustments_success() throws Exception {
        StocktakingSessionDTO result = new StocktakingSessionDTO();
        result.setSessionCode("ST-001");
        result.setStatus("COMPLETED");
        when(service.applyAdjustments(eq("ST-001"), any())).thenReturn(result);

        mockMvc().perform(post("/api/inventory/stocktaking/ST-001/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\":\"Applied\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
