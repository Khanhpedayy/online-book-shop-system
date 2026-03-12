package com.example.onlinebookshop.lot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Lot Service Tests")
class LotServiceTest {

    @Mock
    private LotRepository repo;

    @InjectMocks
    private LotService service;

    @Test
    @DisplayName("getAll no filter → delegates to repo.findAll(null, null)")
    void getAll_noFilter() {
        when(repo.findAll(null, null)).thenReturn(List.of());
        assertTrue(service.getAll(null, null).isEmpty());
        verify(repo).findAll(null, null);
    }

    @Test
    @DisplayName("getAll with supplierId → delegates to repo.findAll(1L, null)")
    void getAll_bySupplierId() {
        when(repo.findAll(1L, null)).thenReturn(List.of(new LotDTO()));
        assertEquals(1, service.getAll(1L, null).size());
    }

    @Test
    @DisplayName("getById found → returns LotDetailDTO with copies")
    void getById_found() {
        LotDTO lot = new LotDTO();
        lot.setId(1L);
        lot.setLotCode("LOT-001");
        lot.setQtyReceived(100);
        lot.setQtyAvailable(80);
        when(repo.findById(1L)).thenReturn(lot);
        when(repo.findCopiesByLot(1L)).thenReturn(List.of());

        LotDetailDTO result = service.getById(1L);
        assertNotNull(result);
        assertEquals("LOT-001", result.getLotCode());
        verify(repo).findCopiesByLot(1L);
    }

    @Test
    @DisplayName("getById not found → throws RuntimeException")
    void getById_notFound() {
        when(repo.findById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getById(999L));
    }

    @Test
    @DisplayName("createLot → calls repo.insert + repo.logTransaction + repo.findById")
    void createLot_success() {
        CreateLotRequest req = new CreateLotRequest();
        req.setLotCode("LOT-001");
        req.setQtyReceived(50);
        req.setVariantId(5L);
        req.setNote("Test note");

        when(repo.insert(req)).thenReturn(10L);
        LotDTO found = new LotDTO();
        found.setId(10L);
        when(repo.findById(10L)).thenReturn(found);

        LotDTO result = service.createLot(req);
        assertEquals(10L, result.getId());
        verify(repo).insert(req);
        verify(repo).logTransaction("IN", 5L, 10L, null, 50, "RECEIPT", 10L, "SALE", "Test note");
    }

    @Test
    @DisplayName("generateCopies → calls repo.generateCopies with lot data")
    void generateCopies_success() {
        LotDTO lot = new LotDTO();
        lot.setId(1L);
        lot.setLotCode("LOT-001");
        lot.setVariantId(5L);
        lot.setQtyReceived(10);
        lot.setConditionDefault("NEW");
        when(repo.findById(1L)).thenReturn(lot);
        when(repo.generateCopies(1L, 5L, 10, "LOT-001-", null, "NEW")).thenReturn(10);

        int count = service.generateCopies(1L, new GenerateCopiesRequest());
        assertEquals(10, count);
    }

    @Test
    @DisplayName("generateCopies lot not found → throws RuntimeException")
    void generateCopies_lotNotFound() {
        when(repo.findById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class,
                () -> service.generateCopies(999L, new GenerateCopiesRequest()));
    }

    @Test
    @DisplayName("lockLot → calls repo.lockLot + logTransaction")
    void lockLot_success() {
        when(repo.lockLot(1L)).thenReturn(1);
        LotDTO lot = new LotDTO();
        lot.setVariantId(5L);
        when(repo.findById(1L)).thenReturn(lot);

        assertDoesNotThrow(() -> service.lockLot(1L, "Recall"));
        verify(repo).lockLot(1L);
        verify(repo).logTransaction("ADJUST", 5L, 1L, null, 0, "ADJUSTMENT", 1L, "LOCKED", "Recall");
    }

    @Test
    @DisplayName("lockLot not found → throws RuntimeException")
    void lockLot_notFound() {
        when(repo.lockLot(999L)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.lockLot(999L, "test"));
    }

    @Test
    @DisplayName("unlockLot → calls repo.unlockLot")
    void unlockLot_success() {
        when(repo.unlockLot(1L)).thenReturn(1);
        assertDoesNotThrow(() -> service.unlockLot(1L));
        verify(repo).unlockLot(1L);
    }

    @Test
    @DisplayName("unlockLot not found → throws RuntimeException")
    void unlockLot_notFound() {
        when(repo.unlockLot(999L)).thenReturn(0);
        assertThrows(RuntimeException.class, () -> service.unlockLot(999L));
    }
}
