package com.example.onlinebookshop.copy;

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
@DisplayName("Copy Service Tests")
class CopyServiceTest {

    @Mock
    private CopyRepository repo;

    @InjectMocks
    private CopyService service;

    @Test
    @DisplayName("search → delegates to repo.search")
    void search() {
        CopyDTO dto = new CopyDTO();
        dto.setCopyCode("CP-001");
        when(repo.search("CP-001")).thenReturn(List.of(dto));
        assertEquals(1, service.search("CP-001").size());
    }

    @Test
    @DisplayName("getAll → delegates to repo.findAll with filters")
    void getAll_filtered() {
        when(repo.findAll(1L, null, "AVAILABLE")).thenReturn(List.of());
        assertTrue(service.getAll(1L, null, "AVAILABLE").isEmpty());
    }

    @Test
    @DisplayName("getLifecycle found → returns from repo.findLifecycleById")
    void getLifecycle_found() {
        CopyLifecycleDTO dto = new CopyLifecycleDTO();
        dto.setId(1L);
        when(repo.findLifecycleById(1L)).thenReturn(dto);

        assertNotNull(service.getLifecycle(1L));
        verify(repo).findLifecycleById(1L);
    }

    @Test
    @DisplayName("getLifecycle not found → throws RuntimeException")
    void getLifecycle_notFound() {
        when(repo.findLifecycleById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.getLifecycle(999L));
    }

    @Test
    @DisplayName("changeCondition → calls repo.updateCondition + logTransaction + findById")
    void changeCondition() {
        ChangeConditionRequest req = new ChangeConditionRequest();
        req.setConditionGrade("GOOD");
        req.setConditionNote("Minor wear");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        existing.setVariantId(5L);
        existing.setLotId(10L);
        existing.setConditionGrade("NEW");

        when(repo.findById(1L)).thenReturn(existing);

        CopyDTO result = service.changeCondition(1L, req);

        verify(repo).updateCondition(1L, "GOOD", "Minor wear");
        verify(repo).logTransaction(eq("ADJUST"), eq(5L), eq(10L), eq(1L), eq(1),
                isNull(), isNull(), eq("ADJUSTMENT"), isNull(), eq("COUNT_DIFF"), anyString());
    }

    @Test
    @DisplayName("moveLocation → calls repo.updateLocation + logTransaction")
    void moveLocation() {
        MoveLocationRequest req = new MoveLocationRequest();
        req.setNewLocation("B2-S5");
        req.setNote("Reorganizing");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        existing.setVariantId(5L);
        existing.setLotId(10L);
        existing.setLocation("A1-S3");

        when(repo.findById(1L)).thenReturn(existing);

        service.moveLocation(1L, req);

        verify(repo).updateLocation(1L, "B2-S5");
        verify(repo).logTransaction("TRANSFER", 5L, 10L, 1L, 1,
                "A1-S3", "B2-S5", "ADJUSTMENT", null, "TRANSFER", "Reorganizing");
    }

    @Test
    @DisplayName("markStatus DAMAGED → calls repo.updateStatus + logTransaction")
    void markStatus_damaged() {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("DAMAGED");
        req.setNote("Water damage");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        existing.setVariantId(5L);
        existing.setLotId(10L);
        when(repo.findById(1L)).thenReturn(existing);

        service.markStatus(1L, req);
        verify(repo).updateStatus(1L, "DAMAGED");
    }

    @Test
    @DisplayName("markStatus LOST → calls repo.updateStatus")
    void markStatus_lost() {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("LOST");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        existing.setVariantId(5L);
        existing.setLotId(10L);
        when(repo.findById(1L)).thenReturn(existing);

        service.markStatus(1L, req);
        verify(repo).updateStatus(1L, "LOST");
    }

    @Test
    @DisplayName("markStatus AVAILABLE (FOUND) → calls repo.updateStatus")
    void markStatus_available() {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("AVAILABLE");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        existing.setVariantId(5L);
        existing.setLotId(10L);
        when(repo.findById(1L)).thenReturn(existing);

        service.markStatus(1L, req);
        verify(repo).updateStatus(1L, "AVAILABLE");
    }

    @Test
    @DisplayName("markStatus INVALID → throws IllegalArgumentException")
    void markStatus_invalid() {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("INVALID");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        when(repo.findById(1L)).thenReturn(existing);

        assertThrows(IllegalArgumentException.class, () -> service.markStatus(1L, req));
    }

    @Test
    @DisplayName("markStatus not found → throws RuntimeException")
    void markStatus_notFound() {
        MarkStatusRequest req = new MarkStatusRequest();
        req.setStatus("DAMAGED");
        when(repo.findById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.markStatus(999L, req));
    }

    @Test
    @DisplayName("attachPhotos → calls repo.updatePhotos + findById")
    void attachPhotos() {
        AttachPhotosRequest req = new AttachPhotosRequest();
        req.setImagesJson("[\"img.jpg\"]");

        CopyDTO existing = new CopyDTO();
        existing.setId(1L);
        when(repo.findById(1L)).thenReturn(existing);

        service.attachPhotos(1L, req);
        verify(repo).updatePhotos(1L, "[\"img.jpg\"]");
    }

    @Test
    @DisplayName("attachPhotos not found → throws RuntimeException")
    void attachPhotos_notFound() {
        AttachPhotosRequest req = new AttachPhotosRequest();
        req.setImagesJson("[]");
        when(repo.findById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.attachPhotos(999L, req));
    }
}
