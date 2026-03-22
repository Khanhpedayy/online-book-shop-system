package com.example.onlinebookshop.adjustment;

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
@DisplayName("Adjustment Service Tests")
class AdjustmentServiceTest {

    @Mock
    private AdjustmentRepository repo;

    @InjectMocks
    private AdjustmentService service;

    @Test
    @DisplayName("getAll → delegates to repo.findAll()")
    void getAll() {
        when(repo.findAll()).thenReturn(List.of(new AdjustmentDTO()));
        assertEquals(1, service.getAll().size());
        verify(repo).findAll();
    }

    @Test
    @DisplayName("createAdjustment DAMAGED → qty_available - abs(qty), qty_damaged + abs(qty)")
    void createAdjustment_damaged() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setLotId(1L);
        req.setQuantity(-5);
        req.setReason("DAMAGED");

        when(repo.insert(req)).thenReturn(100L);

        Long id = service.createAdjustment(req);

        assertEquals(100L, id);
        verify(repo).updateLotQtyAvailable(1L, -5); // -abs(-5) = -5
        verify(repo).updateLotQtyDamaged(1L, 5); // abs(-5) = 5
    }

    @Test
    @DisplayName("createAdjustment LOST → qty_available - abs(qty), no damaged update")
    void createAdjustment_lost() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setLotId(1L);
        req.setQuantity(-3);
        req.setReason("LOST");

        when(repo.insert(req)).thenReturn(101L);

        service.createAdjustment(req);

        verify(repo).updateLotQtyAvailable(1L, -3);
        verify(repo, never()).updateLotQtyDamaged(anyLong(), anyInt());
    }

    @Test
    @DisplayName("createAdjustment FOUND → qty_available + abs(qty)")
    void createAdjustment_found() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setLotId(1L);
        req.setQuantity(2);
        req.setReason("FOUND");

        when(repo.insert(req)).thenReturn(102L);

        service.createAdjustment(req);

        verify(repo).updateLotQtyAvailable(1L, 2); // abs(2)
    }

    @Test
    @DisplayName("createAdjustment COUNT_DIFF → qty_available + raw quantity")
    void createAdjustment_countDiff() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setLotId(1L);
        req.setQuantity(-1);
        req.setReason("COUNT_DIFF");

        when(repo.insert(req)).thenReturn(103L);

        service.createAdjustment(req);

        verify(repo).updateLotQtyAvailable(1L, -1); // raw value, not abs
    }

    @Test
    @DisplayName("createAdjustment without lotId → inserts but skips lot update")
    void createAdjustment_noLotId() {
        CreateAdjustmentRequest req = new CreateAdjustmentRequest();
        req.setQuantity(-1);
        req.setReason("DAMAGED");

        when(repo.insert(req)).thenReturn(104L);

        service.createAdjustment(req);

        verify(repo).insert(req);
        verify(repo, never()).updateLotQtyAvailable(anyLong(), anyInt());
        verify(repo, never()).updateLotQtyDamaged(anyLong(), anyInt());
    }
}
