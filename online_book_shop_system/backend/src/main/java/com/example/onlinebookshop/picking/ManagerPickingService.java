package com.example.onlinebookshop.picking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ManagerPickingService {

    private final ManagerPickingRepository repo;

    public ManagerPickingService(ManagerPickingRepository repo) {
        this.repo = repo;
    }

    public PickListDTO getPickList(Long orderId) {
        List<PickItemDTO> items = repo.findByOrderId(orderId);
        PickListDTO pl = new PickListDTO();
        pl.setOrderId(orderId);
        pl.setTotalItems(items.size());
        pl.setPickedItems((int) items.stream().filter(i -> "PICKED".equals(i.getStatus())).count());
        pl.setItems(items);
        return pl;
    }

    @Transactional
    public void autoAllocate(Long orderId) {
        repo.autoAllocate(orderId);
    }

    @Transactional
    public void manualPick(Long orderId, ManualPickRequest req) {
        Long copyId = repo.findCopyIdByCode(req.getCopyCode());
        if (copyId == null)
            throw new RuntimeException("Copy not found or not available: " + req.getCopyCode());

        // Validate variant matches
        Long copyVariantId = repo.findCopyVariantId(copyId);
        Long itemVariantId = repo.findOrderItemVariantId(req.getOrderItemId());
        if (!copyVariantId.equals(itemVariantId)) {
            throw new IllegalArgumentException("Copy variant does not match order item variant");
        }

        String location = repo.getCopyLocation(copyId);
        repo.insertPickItem(orderId, req.getOrderItemId(), copyId, location, req.getStaffId());
    }

    @Transactional
    public void confirmPick(Long itemId, Long staffId) {
        int updated = repo.confirmPick(itemId, staffId);
        if (updated == 0)
            throw new RuntimeException("Pick item not found or already picked: " + itemId);
    }

    @Transactional
    public void unpick(Long itemId) {
        repo.unpick(itemId);
    }
}

