package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffFulfillmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffDeliveryService {

    private final StaffFulfillmentRepository repo;

    public StaffDeliveryService(StaffFulfillmentRepository repo) {
        this.repo = repo;
    }

    public List<StaffFulfillmentRepository.ShippingQueueRow> getShippingQueue() {
        return repo.findShippingQueue();
    }

    public StaffFulfillmentRepository.DeliveryDetailView getDeliveryDetail(long orderId) {
        return repo.findDeliveryDetail(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn giao hàng."));
    }

    @Transactional
    public void startShipping(long orderId, Long actorUserId) {
        if (!repo.canShip(orderId)) {
            throw new RuntimeException("Đơn chưa đủ điều kiện bắt đầu giao. Phải là PACKED và phiếu phải PACKED, không có lỗi.");
        }

        long stockOutId = repo.findLatestStockOutIdByOrderId(orderId);

        repo.markOrderShipped(orderId, actorUserId);
        repo.markCopiesShippedByStockOut(stockOutId);
        repo.markStockOutOutForDelivery(stockOutId, actorUserId);
        repo.insertInventoryOutByStockOut(stockOutId, orderId);
    }

    @Transactional
    public void confirmDeliveredSuccess(long orderId, Long actorUserId) {
        if (!repo.canDeliver(orderId)) {
            throw new RuntimeException("Đơn chưa ở trạng thái đang giao.");
        }

        long stockOutId = repo.findLatestStockOutIdByOrderId(orderId);

        repo.markOrderDeliveredSuccess(orderId);
        repo.markOrderPaymentPaidIfNeeded(orderId);
        repo.markCopiesSoldByStockOut(stockOutId);
        repo.moveLotReservedToSoldByStockOut(stockOutId);
        repo.markStockOutCompleted(stockOutId);
    }

    @Transactional
    public void confirmDeliveryFail(long orderId, boolean cancelOrder, boolean markReturned, String note) {
        if (!repo.canDeliver(orderId)) {
            throw new RuntimeException("Đơn chưa ở trạng thái đang giao.");
        }

        long stockOutId = repo.findLatestStockOutIdByOrderId(orderId);

        repo.markCopiesReturnedOrAvailableByStockOut(stockOutId, markReturned);
        repo.releaseLotReservedByStockOut(stockOutId);
        repo.insertInventoryReturnByStockOut(stockOutId, orderId, note == null ? "Delivery failed" : note);

        if (cancelOrder) {
            repo.markOrderCancelled(orderId);
            repo.markStockOutCancelled(stockOutId, note == null ? "Delivery failed and order cancelled" : note);
        } else {
            repo.markOrderConfirmedForRetry(orderId);
            repo.markStockOutCancelled(stockOutId, note == null ? "Delivery failed, returned to warehouse" : note);
        }
    }
}