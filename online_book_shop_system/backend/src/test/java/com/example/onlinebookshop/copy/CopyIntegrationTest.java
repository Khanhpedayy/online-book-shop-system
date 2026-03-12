package com.example.onlinebookshop.copy;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Copy Registry
 *
 * ╔═══════════╤════════════════════════════════════════╤═══════════════════╤═════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪════════════════════════════════════════╪═══════════════════╪═════════════════════════╪══════╣
 * ║ IT_CPY_01 │ GET /copies/search?q=TEST-CPY │ q=TEST-CPY │ 200 + list │ N ║
 * ║ IT_CPY_02 │ GET /copies │ — │ 200 + all copies │ N ║
 * ║ IT_CPY_03 │ GET /copies?variantId=90001 │ filter │ 200 + filtered │ N ║
 * ║ IT_CPY_04 │ GET /copies?status=AVAILABLE │ filter │ 200 + filtered │ N ║
 * ║ IT_CPY_05 │ GET /copies/{id} │ id=90001 │ 200 + lifecycle │ N ║
 * ║ IT_CPY_06 │ GET /copies/{id} │ id=99999 │ 404 │ A ║
 * ║ IT_CPY_07 │ PUT /copies/{id}/condition │ LIKE_NEW │ 200 OK │ N ║
 * ║ IT_CPY_08 │ PUT /copies/{id}/condition │ id=99999 │ 404 │ A ║
 * ║ IT_CPY_09 │ PUT /copies/{id}/location │ B-02-03 │ 200 OK │ N ║
 * ║ IT_CPY_10 │ PUT /copies/{id}/status │ DAMAGED │ 200 OK │ N ║
 * ║ IT_CPY_11 │ PUT /copies/{id}/status │ INVALID │ 400 │ A ║
 * ║ IT_CPY_12 │ PUT /copies/{id}/photos │ JSON array │ 200 OK │ N ║
 * ║ IT_CPY_13 │ GET /copies?lotId=90001 │ filter by lot │ 200 + filtered │ N ║
 * ║ IT_CPY_14 │ GET /copies/search?q=NONEXISTENT │ no match │ 200 + empty │ B ║
 * ║ IT_CPY_15 │ PUT /copies/99999/location │ not found │ 404 │ A ║
 * ║ IT_CPY_16 │ PUT /copies/99999/status │ not found │ 404 │ A ║
 * ║ IT_CPY_17 │ PUT /copies/99999/photos │ not found │ 404 │ A ║
 * ║ IT_CPY_18 │ GET /copies?status=SOLD │ filter SOLD │ 200 + filtered │ N ║
 * ║ IT_CPY_19 │ GET /copies?status=DAMAGED │ filter DAMAGED │ 200 + filtered │
 * N ║
 * ║ IT_CPY_20 │ PUT /copies/{id}/condition │ GOOD + note │ 200 OK │ N ║
 * ╚═══════════╧════════════════════════════════════════╧═══════════════════╧═════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Copy Registry")
class CopyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("IT_CPY_01 | GET /copies/search?q=TEST-CPY → 200 + matches")
    void search() throws Exception {
        mockMvc.perform(get("/api/inventory/copies/search").param("q", "TEST-CPY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("IT_CPY_02 | GET /copies → 200 + all copies")
    void getAll() throws Exception {
        mockMvc.perform(get("/api/inventory/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))));
    }

    @Test
    @DisplayName("IT_CPY_03 | GET /copies?variantId=90001 → filtered")
    void getAll_byVariant() throws Exception {
        mockMvc.perform(get("/api/inventory/copies").param("variantId", "90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("IT_CPY_04 | GET /copies?status=AVAILABLE → filtered")
    void getAll_byStatus() throws Exception {
        mockMvc.perform(get("/api/inventory/copies").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("IT_CPY_05 | GET /copies/90001 → 200 + lifecycle")
    void getLifecycle_found() throws Exception {
        mockMvc.perform(get("/api/inventory/copies/90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.copyCode").value("TEST-CPY-001"));
    }

    @Test
    @DisplayName("IT_CPY_06 | GET /copies/99999 → 404")
    void getLifecycle_notFound() throws Exception {
        mockMvc.perform(get("/api/inventory/copies/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_CPY_07 | PUT /copies/90001/condition → LIKE_NEW → 200 OK")
    void changeCondition() throws Exception {
        ChangeConditionRequest req = new ChangeConditionRequest();
        req.setConditionGrade("LIKE_NEW");
        req.setConditionNote("Minor wear after inspection");

        mockMvc.perform(put("/api/inventory/copies/90001/condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_CPY_08 | PUT /copies/99999/condition → 404")
    void changeCondition_notFound() throws Exception {
        ChangeConditionRequest req = new ChangeConditionRequest();
        req.setConditionGrade("GOOD");

        mockMvc.perform(put("/api/inventory/copies/99999/condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("IT_CPY_09 | PUT /copies/90001/location → move to B-02-03 → 200 OK")
    void moveLocation() throws Exception {
        MoveLocationRequest req = new MoveLocationRequest();
        req.setNewLocation("B-02-03");

        mockMvc.perform(put("/api/inventory/copies/90001/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_CPY_10 | PUT /copies/90001/status → DAMAGED → 200 OK")
    void markStatus_damaged() throws Exception {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("DAMAGED");
        req.setReason("Water damage");

        mockMvc.perform(put("/api/inventory/copies/90001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT_CPY_11 | PUT /copies/90001/status → INVALID → 400")
    void markStatus_invalid() throws Exception {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("INVALID_STATUS");

        mockMvc.perform(put("/api/inventory/copies/90001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("IT_CPY_12 | PUT /copies/90001/photos → JSON array → 200 OK")
    void attachPhotos() throws Exception {
        AttachPhotosRequest req = new AttachPhotosRequest();
        req.setImagesJson("[\"https://example.com/photo1.jpg\",\"https://example.com/photo2.jpg\"]");

        mockMvc.perform(put("/api/inventory/copies/90001/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── IT_CPY_13: GET /copies?lotId=90001 → filter by lot ──
    @Test
    @DisplayName("IT_CPY_13 | GET /copies?lotId=90001 → filtered by lot")
    void getAll_byLot() throws Exception {
        mockMvc.perform(get("/api/inventory/copies").param("lotId", "90001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_CPY_14: GET /copies/search?q=NONEXISTENT → empty (Boundary) ──
    @Test
    @DisplayName("IT_CPY_14 | GET /copies/search?q=NONEXISTENT → 200 + empty array")
    void search_noMatch() throws Exception {
        mockMvc.perform(get("/api/inventory/copies/search").param("q", "NONEXISTENT_CODE_XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── IT_CPY_15: PUT /copies/99999/location → 404 ──
    @Test
    @DisplayName("IT_CPY_15 | PUT /copies/99999/location → 404 Not Found")
    void moveLocation_notFound() throws Exception {
        MoveLocationRequest req = new MoveLocationRequest();
        req.setNewLocation("Z-99-99");

        mockMvc.perform(put("/api/inventory/copies/99999/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_CPY_16: PUT /copies/99999/status → 404 ──
    @Test
    @DisplayName("IT_CPY_16 | PUT /copies/99999/status → 404 Not Found")
    void markStatus_notFound() throws Exception {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("DAMAGED");
        req.setReason("Should fail");

        mockMvc.perform(put("/api/inventory/copies/99999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_CPY_17: PUT /copies/99999/photos → 404 ──
    @Test
    @DisplayName("IT_CPY_17 | PUT /copies/99999/photos → 404 Not Found")
    void attachPhotos_notFound() throws Exception {
        AttachPhotosRequest req = new AttachPhotosRequest();
        req.setImagesJson("[\"https://example.com/photo.jpg\"]");

        mockMvc.perform(put("/api/inventory/copies/99999/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── IT_CPY_18: GET /copies?status=SOLD → filter SOLD ──
    @Test
    @DisplayName("IT_CPY_18 | GET /copies?status=SOLD → filtered SOLD copies")
    void getAll_byStatusSold() throws Exception {
        mockMvc.perform(get("/api/inventory/copies").param("status", "SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_CPY_19: GET /copies?status=DAMAGED → filter DAMAGED ──
    @Test
    @DisplayName("IT_CPY_19 | GET /copies?status=DAMAGED → filtered DAMAGED copies")
    void getAll_byStatusDamaged() throws Exception {
        mockMvc.perform(get("/api/inventory/copies").param("status", "DAMAGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ── IT_CPY_20: PUT /copies/{id}/condition → GOOD + detailed note ──
    @Test
    @DisplayName("IT_CPY_20 | PUT /copies/90003/condition → GOOD + note → 200 OK")
    void changeCondition_withDetailedNote() throws Exception {
        ChangeConditionRequest req = new ChangeConditionRequest();
        req.setConditionGrade("GOOD");
        req.setConditionNote("Cover has minor scratches, pages intact, spine crease visible");

        mockMvc.perform(put("/api/inventory/copies/90003/condition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
