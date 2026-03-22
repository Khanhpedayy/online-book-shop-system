package com.example.onlinebookshop.copy;

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
@DisplayName("Copy Registry Controller Tests")
class CopyControllerTest {

    @Mock
    private CopyService service;

    @InjectMocks
    private CopyController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /copies/search?q=CP-001 → 200")
    void search_returnsOk() throws Exception {
        CopyDTO dto = new CopyDTO();
        dto.setId(1L);
        dto.setCopyCode("CP-001");
        when(service.search("CP-001")).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/copies/search").param("q", "CP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].copyCode").value("CP-001"));
    }

    @Test
    @DisplayName("GET /copies → 200 list all")
    void getAll_returnsOk() throws Exception {
        when(service.getAll(null, null, null)).thenReturn(List.of());

        mockMvc().perform(get("/api/inventory/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /copies?variantId=1&status=AVAILABLE → 200 filtered")
    void getAll_filtered() throws Exception {
        CopyDTO dto = new CopyDTO();
        dto.setId(1L);
        dto.setStatus("AVAILABLE");
        when(service.getAll(1L, null, "AVAILABLE")).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/inventory/copies")
                .param("variantId", "1")
                .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /copies/1 → 200 lifecycle detail")
    void getLifecycle_found() throws Exception {
        CopyLifecycleDTO dto = new CopyLifecycleDTO();
        dto.setId(1L);
        dto.setCopyCode("CP-001");
        when(service.getLifecycle(1L)).thenReturn(dto);

        mockMvc().perform(get("/api/inventory/copies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.copyCode").value("CP-001"));
    }

    @Test
    @DisplayName("GET /copies/999 → 404")
    void getLifecycle_notFound() throws Exception {
        when(service.getLifecycle(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/inventory/copies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /copies/1/condition → 200")
    void changeCondition_found() throws Exception {
        ChangeConditionRequest req = new ChangeConditionRequest();
        req.setConditionGrade("GOOD");

        CopyDTO result = new CopyDTO();
        result.setId(1L);
        result.setConditionGrade("GOOD");
        when(service.changeCondition(eq(1L), any())).thenReturn(result);

        mockMvc().perform(put("/api/inventory/copies/1/condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conditionGrade").value("GOOD"));
    }

    @Test
    @DisplayName("PUT /copies/1/location → 200")
    void moveLocation_found() throws Exception {
        MoveLocationRequest req = new MoveLocationRequest();
        req.setNewLocation("A1-S3");

        CopyDTO result = new CopyDTO();
        result.setId(1L);
        when(service.moveLocation(eq(1L), any())).thenReturn(result);

        mockMvc().perform(put("/api/inventory/copies/1/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /copies/1/status DAMAGED → 200")
    void markStatus_damaged() throws Exception {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("DAMAGED");
        req.setReason("Water damage");

        CopyDTO result = new CopyDTO();
        result.setId(1L);
        result.setStatus("DAMAGED");
        when(service.markStatus(eq(1L), any())).thenReturn(result);

        mockMvc().perform(put("/api/inventory/copies/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DAMAGED"));
    }

    @Test
    @DisplayName("PUT /copies/1/status invalid → 400")
    void markStatus_invalid() throws Exception {
        when(service.markStatus(eq(1L), any()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        mockMvc().perform(put("/api/inventory/copies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /copies/1/photos → 200")
    void attachPhotos_found() throws Exception {
        AttachPhotosRequest req = new AttachPhotosRequest();
        req.setImagesJson("[\"img1.jpg\",\"img2.jpg\"]");

        CopyDTO result = new CopyDTO();
        result.setId(1L);
        when(service.attachPhotos(eq(1L), any())).thenReturn(result);

        mockMvc().perform(put("/api/inventory/copies/1/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /copies/999/photos → 404")
    void attachPhotos_notFound() throws Exception {
        when(service.attachPhotos(eq(999L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(put("/api/inventory/copies/999/photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imagesJson\":\"[]\"}"))
                .andExpect(status().isNotFound());
    }
}
