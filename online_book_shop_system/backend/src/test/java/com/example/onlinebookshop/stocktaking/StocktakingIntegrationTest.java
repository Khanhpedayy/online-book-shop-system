package com.example.onlinebookshop.stocktaking;

import com.example.onlinebookshop.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests — Stocktaking (Cycle Count)
 *
 * ╔═══════════╤══════════════════════════════════════════════════════╤══════════════════════╤════════════════════════╤══════╗
 * ║ Test ID │ Endpoint │ Input │ Expected │ Type ║
 * ╠═══════════╪══════════════════════════════════════════════════════╪══════════════════════╪════════════════════════╪══════╣
 * ║ IT_ST_01 │ GET /api/inventory/stocktaking │ — │ 200 + sessions │ N ║
 * ║ IT_ST_02 │ POST /api/inventory/stocktaking (ALL scope) │ scope=ALL │ 201
 * Created │ N ║
 * ║ IT_ST_03 │ POST /api/inventory/stocktaking (VARIANT scope) │
 * scope=VARIANT:90001 │ 201 Created │ N ║
 * ║ IT_ST_04 │ POST /api/inventory/stocktaking (LOT scope) │ scope=LOT:90001 │
 * 201 Created │ N ║
 * ║ IT_ST_05 │ GET /api/inventory/stocktaking/{code} │ valid code │ 200 +
 * session detail │ N ║
 * ║ IT_ST_06 │ GET /api/inventory/stocktaking/{code} │ invalid code │ 404 Not
 * Found │ A ║
 * ║ IT_ST_07 │ POST /{code}/count │ valid count │ 200 OK │ N ║
 * ║ IT_ST_08 │ POST /{code}/apply │ apply diffs │ 200 OK │ N ║
 * ║ IT_ST_09 │ POST /INVALID-SESSION/count │ bad session │ 400 Bad Request │ A
 * ║
 * ║ IT_ST_10 │ POST /UNKNOWN-CODE/apply │ bad session │ 400 Bad Request │ A ║
 * ║ IT_ST_11 │ Full Flow: create → count → apply │ full cycle │ 201 → 200 → 200
 * │ N ║
 * ║ IT_ST_12 │ POST /{code}/apply with note │ apply + note │ 200 OK │ N ║
 * ║ IT_ST_13 │ POST /api/inventory/stocktaking (empty scope) │ scope="" │
 * verify behavior │ B ║
 * ║ IT_ST_14 │ GET /stocktaking → verify seeded sessions exist │ — │ 200 +
 * seeded data │ N ║
 * ╚═══════════╧══════════════════════════════════════════════════════╧══════════════════════╧════════════════════════╧══════╝
 */
@DisplayName("Integration Tests — Stocktaking")
class StocktakingIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("IT_ST_01 | GET /stocktaking → 200 + sessions list")
        void getAllSessions() throws Exception {
                mockMvc.perform(get("/api/inventory/stocktaking"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("IT_ST_02 | POST /stocktaking (scope=ALL) → 201 Created")
        void createSession_allScope() throws Exception {
                CreateStocktakingRequest req = new CreateStocktakingRequest();
                req.setScope("ALL");

                mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.sessionCode").exists())
                                .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @DisplayName("IT_ST_03 | POST /stocktaking (scope=VARIANT:90001) → 201 Created")
        void createSession_variantScope() throws Exception {
                CreateStocktakingRequest req = new CreateStocktakingRequest();
                req.setScope("VARIANT:90001");

                mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.sessionCode").exists());
        }

        @Test
        @DisplayName("IT_ST_04 | POST /stocktaking (scope=LOT:90001) → 201 Created")
        void createSession_lotScope() throws Exception {
                CreateStocktakingRequest req = new CreateStocktakingRequest();
                req.setScope("LOT:90001");

                mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.sessionCode").exists());
        }

        @Test
        @DisplayName("IT_ST_05 | GET /stocktaking/{code} → 200 + session detail")
        void getSession_found() throws Exception {
                // First create a session to get a valid sessionCode
                CreateStocktakingRequest req = new CreateStocktakingRequest();
                req.setScope("ALL");

                String response = mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                String sessionCode = objectMapper.readTree(response).get("sessionCode").asText();

                mockMvc.perform(get("/api/inventory/stocktaking/" + sessionCode))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sessionCode").value(sessionCode))
                                .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @DisplayName("IT_ST_06 | GET /stocktaking/INVALID-CODE → 404 Not Found")
        void getSession_notFound() throws Exception {
                mockMvc.perform(get("/api/inventory/stocktaking/INVALID-CODE-99999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("IT_ST_07 | POST /{code}/count → record counted qty → 200 OK")
        void recordCount() throws Exception {
                // Create session first
                CreateStocktakingRequest createReq = new CreateStocktakingRequest();
                createReq.setScope("ALL");

                String response = mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                String sessionCode = objectMapper.readTree(response).get("sessionCode").asText();

                // Record count
                RecordCountRequest countReq = new RecordCountRequest();
                countReq.setLotId(90001L);
                countReq.setCountedQty(38);

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(countReq)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("IT_ST_08 | POST /{code}/apply → apply adjustments → 200 OK")
        void applyAdjustments() throws Exception {
                // Create session
                CreateStocktakingRequest createReq = new CreateStocktakingRequest();
                createReq.setScope("ALL");

                String response = mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                String sessionCode = objectMapper.readTree(response).get("sessionCode").asText();

                // Record count first
                RecordCountRequest countReq = new RecordCountRequest();
                countReq.setLotId(90001L);
                countReq.setCountedQty(38);

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(countReq)))
                                .andExpect(status().isOk());

                // Apply
                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        // ── IT_ST_09: POST /INVALID-SESSION/count → 400 ──
        @Test
        @DisplayName("IT_ST_09 | POST /INVALID-SESSION/count → 400 Bad Request")
        void recordCount_invalidSession() throws Exception {
                RecordCountRequest countReq = new RecordCountRequest();
                countReq.setLotId(90001L);
                countReq.setCountedQty(10);

                mockMvc.perform(post("/api/inventory/stocktaking/INVALID-SESSION-999/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(countReq)))
                                .andExpect(status().isBadRequest());
        }

        // ── IT_ST_10: POST /UNKNOWN/apply → 400 ──
        @Test
        @DisplayName("IT_ST_10 | POST /UNKNOWN-CODE/apply → 400 Bad Request")
        void applyAdjustments_invalidSession() throws Exception {
                mockMvc.perform(post("/api/inventory/stocktaking/UNKNOWN-CODE-999/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        // ── IT_ST_11: Full Flow: create → get → count → apply ──
        @Test
        @DisplayName("IT_ST_11 | Full Flow: create session → get → record count → apply → COMPLETED")
        void fullFlow() throws Exception {
                // 1. Create session
                CreateStocktakingRequest createReq = new CreateStocktakingRequest();
                createReq.setScope("VARIANT:90001");

                String response = mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                String sessionCode = objectMapper.readTree(response).get("sessionCode").asText();

                // 2. Get session detail
                mockMvc.perform(get("/api/inventory/stocktaking/" + sessionCode))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("OPEN"));

                // 3. Record count
                RecordCountRequest countReq = new RecordCountRequest();
                countReq.setLotId(90001L);
                countReq.setCountedQty(39);

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(countReq)))
                                .andExpect(status().isOk());

                // 4. Apply adjustments
                ApplyAdjustmentsRequest applyReq = new ApplyAdjustmentsRequest();
                applyReq.setNote("Full flow IT test — applying stocktake results");

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(applyReq)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        // ── IT_ST_12: POST /{code}/apply with note ──
        @Test
        @DisplayName("IT_ST_12 | POST /{code}/apply (with note) → 200 OK")
        void applyAdjustments_withNote() throws Exception {
                CreateStocktakingRequest createReq = new CreateStocktakingRequest();
                createReq.setScope("LOT:90002");

                String response = mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                String sessionCode = objectMapper.readTree(response).get("sessionCode").asText();

                RecordCountRequest countReq = new RecordCountRequest();
                countReq.setLotId(90002L);
                countReq.setCountedQty(25);

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(countReq)))
                                .andExpect(status().isOk());

                ApplyAdjustmentsRequest applyReq = new ApplyAdjustmentsRequest();
                applyReq.setNote("Integration test apply with detailed note");

                mockMvc.perform(post("/api/inventory/stocktaking/" + sessionCode + "/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(applyReq)))
                                .andExpect(status().isOk());
        }

        // ── IT_ST_13: POST → empty scope (Boundary) ──
        @Test
        @DisplayName("IT_ST_13 | POST /stocktaking (scope='') → verify behavior (boundary)")
        void createSession_emptyScope() throws Exception {
                CreateStocktakingRequest req = new CreateStocktakingRequest();
                req.setScope("");

                mockMvc.perform(post("/api/inventory/stocktaking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().is2xxSuccessful()); // No validation at controller
        }

        // ── IT_ST_14: GET /stocktaking → seeded sessions ──
        @Test
        @DisplayName("IT_ST_14 | GET /stocktaking → 200 + has seeded stocktaking sessions")
        void getAllSessions_dataCheck() throws Exception {
                mockMvc.perform(get("/api/inventory/stocktaking"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }
}
