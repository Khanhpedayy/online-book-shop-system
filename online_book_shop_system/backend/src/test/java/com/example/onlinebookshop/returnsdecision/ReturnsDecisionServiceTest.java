package com.example.onlinebookshop.returnsdecision;

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
@DisplayName("Returns Decision Service Tests")
class ReturnsDecisionServiceTest {

    @Mock
    private ReturnsDecisionRepository repo;

    @InjectMocks
    private ReturnsDecisionService service;

    private ReturnOverviewDTO makeReturn(Long itemId, Long copyId) {
        ReturnItemDTO item = new ReturnItemDTO();
        item.setId(itemId);
        item.setCopyId(copyId);

        ReturnOverviewDTO ret = new ReturnOverviewDTO();
        ret.setReturnId(1L);
        List<ReturnItemDTO> items = new ArrayList<>();
        items.add(item);
        ret.setItems(items);
        return ret;
    }

    @Test
    @DisplayName("getAllReturns → delegates to repo.findAllReturns()")
    void getAllReturns() {
        when(repo.findAllReturns()).thenReturn(List.of(new ReturnOverviewDTO()));
        assertEquals(1, service.getAllReturns().size());
        verify(repo).findAllReturns();
    }

    @Test
    @DisplayName("processReturnItem RESTOCK → updateCopyStatus AVAILABLE + logTransaction")
    void processReturnItem_restock() {
        ReturnOverviewDTO ret = makeReturn(10L, 50L);
        when(repo.findItemsByReturn(null)).thenReturn(List.of());
        when(repo.findAllReturns()).thenReturn(List.of(ret));

        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");
        req.setNote("Good condition");

        service.processReturnItem(10L, req);

        verify(repo).processReturnItem(10L, "RESTOCK");
        verify(repo).updateCopyStatus(50L, "AVAILABLE");
        verify(repo).logTransaction("RETURN", null, null, 50L, 1, "RETURN", 10L, "SALE",
                "Restock: Good condition");
    }

    @Test
    @DisplayName("processReturnItem RESTOCK_REPRICE → updateCopyStatus + updateCopyCondition + updateCopyPriceOverride")
    void processReturnItem_restockReprice() {
        ReturnOverviewDTO ret = makeReturn(10L, 50L);
        when(repo.findItemsByReturn(null)).thenReturn(List.of());
        when(repo.findAllReturns()).thenReturn(List.of(ret));

        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK_REPRICE");
        req.setConditionGrade("GOOD");
        req.setNewSellPrice(15.0);
        req.setNote("Minor wear");

        service.processReturnItem(10L, req);

        verify(repo).processReturnItem(10L, "RESTOCK_REPRICE");
        verify(repo).updateCopyStatus(50L, "AVAILABLE");
        verify(repo).updateCopyCondition(50L, "GOOD");
        verify(repo).updateCopyPriceOverride(50L, 15.0);
    }

    @Test
    @DisplayName("processReturnItem DAMAGED → updateCopyStatus DAMAGED + logTransaction ADJUST")
    void processReturnItem_damaged() {
        ReturnOverviewDTO ret = makeReturn(10L, 50L);
        when(repo.findItemsByReturn(null)).thenReturn(List.of());
        when(repo.findAllReturns()).thenReturn(List.of(ret));

        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("DAMAGED");
        req.setNote("Torn pages");

        service.processReturnItem(10L, req);

        verify(repo).updateCopyStatus(50L, "DAMAGED");
        verify(repo).logTransaction("ADJUST", null, null, 50L, 1, "RETURN", 10L, "DAMAGED", "Torn pages");
    }

    @Test
    @DisplayName("processReturnItem SUPPLIER_RETURN → updateCopyStatus RETURNED + logTransaction OUT")
    void processReturnItem_supplierReturn() {
        ReturnOverviewDTO ret = makeReturn(10L, 50L);
        when(repo.findItemsByReturn(null)).thenReturn(List.of());
        when(repo.findAllReturns()).thenReturn(List.of(ret));

        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("SUPPLIER_RETURN");
        req.setNote("Defective batch");

        service.processReturnItem(10L, req);

        verify(repo).updateCopyStatus(50L, "RETURNED");
    }

    @Test
    @DisplayName("processReturnItem item not found → throws RuntimeException")
    void processReturnItem_itemNotFound() {
        when(repo.findItemsByReturn(null)).thenReturn(List.of());
        when(repo.findAllReturns()).thenReturn(List.of());

        ProcessReturnItemRequest req = new ProcessReturnItemRequest();
        req.setAction("RESTOCK");

        assertThrows(RuntimeException.class, () -> service.processReturnItem(999L, req));
    }
}
