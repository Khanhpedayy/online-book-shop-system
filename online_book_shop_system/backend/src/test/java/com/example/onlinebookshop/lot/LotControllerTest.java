package com.example.onlinebookshop.lot;

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
@DisplayName("Lot Controller Tests")
class LotControllerTest {

    @Mock
    private LotService service;

    @InjectMocks
    private LotController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /lots → 200 list all")
    void getAll_noFilter() throws Exception {
        LotDTO dto = new LotDTO();
        dto.setId(1L);
        dto.setLotCode("LOT-001");
        when(service.getAll(null, null)).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lotCode").value("LOT-001"));
    }

    @Test
    @DisplayName("GET /lots?supplierId=1 → 200 filtered")
    void getAll_bySupplierId() throws Exception {
        when(service.getAll(1L, null)).thenReturn(List.of());

        mockMvc().perform(get("/api/inventory/lots").param("supplierId", "1"))
                .andExpect(status().isOk());
        verify(service).getAll(1L, null);
    }

    @Test
    @DisplayName("GET /lots/1 → 200 detail view")
    void getById_found() throws Exception {
        LotDetailDTO dto = new LotDetailDTO();
        dto.setId(1L);
        dto.setQtyReceived(100);
        dto.setQtyAvailable(80);
        when(service.getById(1L)).thenReturn(dto);

        mockMvc().perform(get("/api/inventory/lots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyAvailable").value(80));
    }

    @Test
    @DisplayName("GET /lots/999 → 404")
    void getById_notFound() throws Exception {
        when(service.getById(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/inventory/lots/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /lots → 201 Created")
    void create_returnsCreated() throws Exception {
        CreateLotRequest req = new CreateLotRequest();
        req.setLotCode("LOT-NEW");
        req.setSupplierId(1L);
        req.setVariantId(1L);

        LotDTO created = new LotDTO();
        created.setId(10L);
        created.setLotCode("LOT-NEW");
        when(service.createLot(any())).thenReturn(created);

        mockMvc().perform(post("/api/inventory/lots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lotCode").value("LOT-NEW"));
    }

    @Test
    @DisplayName("POST /lots/1/generate-copies → 200 with count")
    void generateCopies_success() throws Exception {
        GenerateCopiesRequest req = new GenerateCopiesRequest();
        req.setPrefix("CP");
        when(service.generateCopies(eq(1L), any())).thenReturn(50);

        mockMvc().perform(post("/api/inventory/lots/1/generate-copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated").value(50));
    }

    @Test
    @DisplayName("POST /lots/999/generate-copies → 404")
    void generateCopies_lotNotFound() throws Exception {
        when(service.generateCopies(eq(999L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(post("/api/inventory/lots/999/generate-copies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /lots/1/lock → 200")
    void lock_success() throws Exception {
        doNothing().when(service).lockLot(eq(1L), any());

        mockMvc().perform(put("/api/inventory/lots/1/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Recall\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /lots/1/unlock → 200")
    void unlock_success() throws Exception {
        doNothing().when(service).unlockLot(1L);

        mockMvc().perform(put("/api/inventory/lots/1/unlock"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /lots/999/lock → 404")
    void lock_notFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(service).lockLot(eq(999L), any());

        mockMvc().perform(put("/api/inventory/lots/999/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"test\"}"))
                .andExpect(status().isNotFound());
    }
}
