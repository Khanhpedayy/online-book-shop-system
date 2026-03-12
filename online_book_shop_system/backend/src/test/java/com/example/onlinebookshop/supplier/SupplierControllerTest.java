package com.example.onlinebookshop.supplier;

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
@DisplayName("Supplier Controller Tests")
class SupplierControllerTest {

    @Mock
    private SupplierService service;

    @InjectMocks
    private SupplierController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /suppliers → 200 list all")
    void getAll_returnsOk() throws Exception {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("ABC Books");
        when(service.getAll()).thenReturn(List.of(dto));

        mockMvc().perform(get("/api/management/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ABC Books"));
    }

    @Test
    @DisplayName("GET /suppliers/1 → 200 found")
    void getById_found() throws Exception {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("ABC Books");
        when(service.getById(1L)).thenReturn(dto);

        mockMvc().perform(get("/api/management/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC Books"));
    }

    @Test
    @DisplayName("GET /suppliers/999 → 404")
    void getById_notFound() throws Exception {
        when(service.getById(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(get("/api/management/suppliers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /suppliers → 201 Created")
    void create_returnsCreated() throws Exception {
        CreateSupplierRequest req = new CreateSupplierRequest();
        req.setName("New Supplier");
        req.setCode("SUP-001");

        SupplierDTO created = new SupplierDTO();
        created.setId(10L);
        created.setName("New Supplier");
        when(service.create(any())).thenReturn(created);

        mockMvc().perform(post("/api/management/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Supplier"));
    }

    @Test
    @DisplayName("PUT /suppliers/1 → 200 OK")
    void update_found() throws Exception {
        SupplierDTO updated = new SupplierDTO();
        updated.setId(1L);
        updated.setName("Updated Name");
        when(service.update(eq(1L), any())).thenReturn(updated);

        mockMvc().perform(put("/api/management/suppliers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /suppliers/999 → 404")
    void update_notFound() throws Exception {
        when(service.update(eq(999L), any())).thenThrow(new RuntimeException("Not found"));

        mockMvc().perform(put("/api/management/suppliers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /suppliers/1 → 204")
    void delete_found() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc().perform(delete("/api/management/suppliers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /suppliers/999 → 404")
    void delete_notFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(service).delete(999L);

        mockMvc().perform(delete("/api/management/suppliers/999"))
                .andExpect(status().isNotFound());
    }
}
