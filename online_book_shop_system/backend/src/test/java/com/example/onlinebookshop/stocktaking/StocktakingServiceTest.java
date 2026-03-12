package com.example.onlinebookshop.stocktaking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stocktaking Service Tests")
class StocktakingServiceTest {

    @Mock
    private StocktakingRepository repo;

    @InjectMocks
    private StocktakingService service;

    @Test
    @DisplayName("getAllSessions → delegates to repo.getAllSessions()")
    void getAllSessions() {
        when(repo.getAllSessions()).thenReturn(List.of());
        assertTrue(service.getAllSessions().isEmpty());
        verify(repo).getAllSessions();
    }

    @Test
    @DisplayName("getSession found → returns session from repo.getSession()")
    void getSession_found() {
        StocktakingSessionDTO dto = new StocktakingSessionDTO();
        dto.setSessionCode("ST-ABC");
        when(repo.getSession("ST-ABC")).thenReturn(dto);

        assertNotNull(service.getSession("ST-ABC"));
        verify(repo).getSession("ST-ABC");
    }

    @Test
    @DisplayName("getSession not found → throws RuntimeException")
    void getSession_notFound() {
        when(repo.getSession("INVALID")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getSession("INVALID"));
    }

    @Test
    @DisplayName("createSession → calls getExpectedStock + saveSession, generates session code")
    void createSession() {
        CreateStocktakingRequest req = new CreateStocktakingRequest();
        req.setScope("ALL");
        req.setNote("Monthly count");

        StocktakingEntryDTO entry = new StocktakingEntryDTO();
        entry.setVariantId(1L);
        entry.setExpectedQty(100);
        when(repo.getExpectedStock("ALL")).thenReturn(List.of(entry));

        StocktakingSessionDTO result = service.createSession(req);

        assertNotNull(result);
        assertTrue(result.getSessionCode().startsWith("ST-"));
        assertEquals("OPEN", result.getStatus());
        assertEquals(1, result.getEntries().size());
        verify(repo).saveSession(any(StocktakingSessionDTO.class));
    }

    @Test
    @DisplayName("recordCount → updates entry and saves session")
    void recordCount() {
        StocktakingEntryDTO entry = new StocktakingEntryDTO();
        entry.setVariantId(1L);
        entry.setLotId(10L);
        entry.setExpectedQty(100);

        StocktakingSessionDTO session = new StocktakingSessionDTO();
        session.setSessionCode("ST-TEST");
        session.setStatus("OPEN");
        List<StocktakingEntryDTO> entries = new ArrayList<>();
        entries.add(entry);
        session.setEntries(entries);

        when(repo.getSession("ST-TEST")).thenReturn(session);

        RecordCountRequest req = new RecordCountRequest();
        req.setVariantId(1L);
        req.setLotId(10L);
        req.setCountedQty(95);

        StocktakingSessionDTO result = service.recordCount("ST-TEST", req);

        assertEquals(95, result.getEntries().get(0).getCountedQty());
        assertEquals(-5, result.getEntries().get(0).getDiff());
        verify(repo).saveSession(session);
    }

    @Test
    @DisplayName("recordCount completed session → throws RuntimeException")
    void recordCount_completed() {
        StocktakingSessionDTO session = new StocktakingSessionDTO();
        session.setSessionCode("ST-TEST");
        session.setStatus("COMPLETED");

        when(repo.getSession("ST-TEST")).thenReturn(session);

        RecordCountRequest req = new RecordCountRequest();
        assertThrows(RuntimeException.class, () -> service.recordCount("ST-TEST", req));
    }

    @Test
    @DisplayName("applyAdjustments → updates lots + logs adjustments + marks COMPLETED")
    void applyAdjustments() {
        StocktakingEntryDTO entry = new StocktakingEntryDTO();
        entry.setVariantId(1L);
        entry.setLotId(10L);
        entry.setExpectedQty(100);
        entry.setCountedQty(95);
        entry.setDiff(-5);

        StocktakingSessionDTO session = new StocktakingSessionDTO();
        session.setSessionCode("ST-TEST");
        session.setStatus("OPEN");
        List<StocktakingEntryDTO> entries = new ArrayList<>();
        entries.add(entry);
        session.setEntries(entries);

        when(repo.getSession("ST-TEST")).thenReturn(session);

        StocktakingSessionDTO result = service.applyAdjustments("ST-TEST", "Monthly adjustment");

        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(repo).updateLotQtyAvailable(10L, -5);
        verify(repo).logAdjustment(eq(1L), eq(10L), eq(-5), contains("ST-TEST"));
        verify(repo).saveSession(session);
    }

    @Test
    @DisplayName("applyAdjustments completed session → throws RuntimeException")
    void applyAdjustments_completed() {
        StocktakingSessionDTO session = new StocktakingSessionDTO();
        session.setSessionCode("ST-TEST");
        session.setStatus("COMPLETED");

        when(repo.getSession("ST-TEST")).thenReturn(session);

        assertThrows(RuntimeException.class,
                () -> service.applyAdjustments("ST-TEST", "note"));
    }
}
