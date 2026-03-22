package com.example.onlinebookshop.returnsdecision;

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
@DisplayName("Returns Decision Controller Tests")
class ReturnsDecisionControllerTest {

    @Mock
    private ReturnsDecisionService service;

    @InjectMocks
    private ReturnsDecisionController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /returns → 200 list all returns")
    void getAll_returnsOk() throws Exception {
        ReturnOverviewDTO dto = new ReturnOverviewDTO();
        dto.setReturnId(1L);
        when(service.getAllReturns()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].returnId").value(1));
    }

    @Test
    @DisplayName("PUT /returns/items/1/process RESTOCK → 200")
    void processItem_restock() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");

        doNothing().when(service).processReturnItem(eq(1L), any());

        mockMvc().perform(put("/api/inventory/returns/items/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /returns/items/1/process RESTOCK_REPRICE → 200")
    void processItem_restockReprice() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK_REPRICE");
        req.setConditionGrade("GOOD");
        req.setNewSellPrice(15.99);

        doNothing().when(service).processReturnItem(eq(1L), any());

        mockMvc().perform(put("/api/inventory/returns/items/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /returns/items/1/process DAMAGED → 200")
    void processItem_damaged() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("DAMAGED");
        req.setNote("Books are water damaged");

        doNothing().when(service).processReturnItem(eq(1L), any());

        mockMvc().perform(put("/api/inventory/returns/items/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /returns/items/1/process SUPPLIER_RETURN → 200")
    void processItem_supplierReturn() throws Exception {
        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("SUPPLIER_RETURN");

        doNothing().when(service).processReturnItem(eq(1L), any());

        mockMvc().perform(put("/api/inventory/returns/items/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /returns/items/999/process → 404")
    void processItem_notFound() throws Exception {
        doThrow(new RuntimeException("Not found"))
                .when(service).processReturnItem(eq(999L), any());

        mockMvc().perform(put("/api/inventory/returns/items/999/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"RESTOCK\"}"))
                .andExpect(status().isNotFound());
    }
}
