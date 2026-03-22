package com.example.onlinebookshop.allocation;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Allocation Settings
 *
 * ╔═══════════╤════════════════════════════════════════╤══════════════════════╤══════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════════════╪══════════════════════╪══════════════════════════╪══════╣
 * ║ IT_ALC_01 │ GET /api/settings/allocation │ — │ 200 + default config │ N ║
 * ║ IT_ALC_02 │ PUT /api/settings/allocation │ valid │ 200 + updated │ N ║
 * ║ IT_ALC_03 │ PUT then GET /api/settings/allocation │ modified │ Persisted
 * changes │ N ║
 * ║ IT_ALC_04 │ PUT /api/settings/allocation (boundary)│ ttl=0 │ 200 OK (edge
 * case) │ B ║
 * ║ IT_ALC_05 │ PUT /api/settings/allocation │ negative TTL │ verify behavior │
 * B ║
 * ║ IT_ALC_06 │ PUT /api/settings/allocation │ different FIFO modes │ 200 OK │
 * N ║
 * ║ IT_ALC_07 │ GET /api/settings/allocation │ field existence │ 200 + all
 * fields exist │ N ║
 * ║ IT_ALC_08 │ PUT /api/settings/allocation │ large TTL=99999 │ 200 OK
 * (boundary) │ B ║
 * ╚═══════════╧════════════════════════════════════════╧══════════════════════╧══════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Allocation Settings")
class AllocationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_ALC_01 | GET /api/settings/allocation → 200 + default settings")
    void getSettings() throws Exception {
        mockMvc.perform(get("/api/settings/allocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").exists())
                .andExpect(jsonPath("$.reservationTtlMin").exists());
    }

    @Test
    @DisplayName("IT_ALC_02 | PUT /api/settings/allocation → 200 + updated")
    void updateSettings() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("COPY", 60, "OLDEST_FIRST", true);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").value("COPY"))
                .andExpect(jsonPath("$.reservationTtlMin").value(60));
    }

    @Test
    @DisplayName("IT_ALC_03 | PUT then GET → persisted correctly")
    void updateThenGet() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("LOT", 45, "CHEAPEST_FIRST", false);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/settings/allocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").value("LOT"))
                .andExpect(jsonPath("$.reservationTtlMin").value(45))
                .andExpect(jsonPath("$.conditionPriority").value("CHEAPEST_FIRST"));
    }

    @Test
    @DisplayName("IT_ALC_04 | PUT /api/settings/allocation (boundary: ttl=0) → 200 OK")
    void updateSettings_boundaryZeroTtl() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("LOT", 0, "NEWEST_FIRST", false);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationTtlMin").value(0));
    }

    // ── IT_ALC_05: PUT → negative TTL (Boundary) ──
    @Test
    @DisplayName("IT_ALC_05 | PUT /api/settings/allocation (ttl=-10) → boundary")
    void updateSettings_negativeTtl() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("LOT", -10, "OLDEST_FIRST", false);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()); // No validation at controller level
    }

    // ── IT_ALC_06: PUT → different FIFO modes ──
    @Test
    @DisplayName("IT_ALC_06 | PUT /api/settings/allocation (COPY mode) → 200 OK")
    void updateSettings_copyFifo() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("COPY", 30, "OLDEST_FIRST", true);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").value("COPY"))
                .andExpect(jsonPath("$.allowStaffOverride").value(true));
    }

    // ── IT_ALC_07: GET → all fields exist ──
    @Test
    @DisplayName("IT_ALC_07 | GET /api/settings/allocation → all fields present")
    void getSettings_allFieldsExist() throws Exception {
        mockMvc.perform(get("/api/settings/allocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fifoBy").exists())
                .andExpect(jsonPath("$.reservationTtlMin").exists())
                .andExpect(jsonPath("$.conditionPriority").exists())
                .andExpect(jsonPath("$.allowStaffOverride").exists());
    }

    // ── IT_ALC_08: PUT → large TTL (Boundary) ──
    @Test
    @DisplayName("IT_ALC_08 | PUT /api/settings/allocation (ttl=99999) → boundary")
    void updateSettings_largeTtl() throws Exception {
        AllocationSettingsDTO dto = new AllocationSettingsDTO("LOT", 99999, "OLDEST_FIRST", false);

        mockMvc.perform(put("/api/settings/allocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationTtlMin").value(99999));
    }
}
