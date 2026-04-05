package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffFulfillmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffPackingService {

    private final StaffFulfillmentRepository repo;

    public StaffPackingService(StaffFulfillmentRepository repo) {
        this.repo = repo;
    }

    public List<StaffFulfillmentRepository.PackingQueueRow> getPackingQueue() {
        return repo.findPackingQueue();
    }

    public StaffFulfillmentRepository.PackDetailView getPackDetail(long orderId) {
        return repo.findPackDetail(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn/phiếu để đóng gói."));
    }

    @Transactional
    public void confirmPacked(long orderId, Long actorUserId) {
        if (!repo.canPack(orderId)) {
            throw new RuntimeException("Đơn chưa đủ điều kiện đóng gói. Phiếu phải ở trạng thái PICKED và không có ngoại lệ.");
        }

        long stockOutId = repo.findLatestStockOutIdByOrderId(orderId);

        repo.markOrderPacked(orderId, actorUserId);
        repo.markCopiesPackedByStockOut(stockOutId);
        repo.markStockOutPacked(stockOutId, actorUserId);
    }
}