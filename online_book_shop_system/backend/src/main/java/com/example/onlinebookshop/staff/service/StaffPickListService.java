package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.PickListView;
import com.example.onlinebookshop.staff.repo.StaffPickListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffPickListService {

    private final StaffPickListRepository repo;

    public StaffPickListService(StaffPickListRepository repo) {
        this.repo = repo;
    }

    public PickListView getPickList(long orderId) {
        PickListView v = repo.getOrderHeader(orderId);
        v.setItems(repo.getPickListItems(orderId));
        v.setTotalAllocated(repo.countAllocated(orderId));
        v.setTotalPicked(repo.countPicked(orderId));
        return v;
    }

    @Transactional
    public void scanConfirmPicked(long orderId, String copyCode) {
        if (copyCode == null || copyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("copyCode is required");
        }
        String code = copyCode.trim();

        long copyId = repo.findCopyIdByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy copyCode=" + code));

        long orderItemId = repo.findOrderItemIdByOrderAndCopy(orderId, copyId)
                .orElseThrow(() -> new IllegalArgumentException("Copy này không thuộc order hoặc chưa được allocate cho order."));

        // set picked_at nếu chưa picked
        int r1 = repo.markOrderItemPicked(orderItemId, "SCAN");
        // set copy status -> PICKED
        repo.markCopyPicked(copyId);

        // r1=0 nghĩa là đã picked rồi -> vẫn coi như OK
    }
}