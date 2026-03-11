package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.OrderItemRow;
import com.example.onlinebookshop.staff.dto.ReturnIntakeView;
import com.example.onlinebookshop.staff.repo.StaffOrderQueryRepository;
import com.example.onlinebookshop.staff.repo.StaffReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StaffReturnService {

    private final StaffReturnRepository repo;
    private final StaffOrderQueryRepository orderQuery;

    public StaffReturnService(StaffReturnRepository repo, StaffOrderQueryRepository orderQuery) {
        this.repo = repo;
        this.orderQuery = orderQuery;
    }

    public ReturnIntakeView buildCreateScreen(long orderId) {
        ReturnIntakeView v = repo.getCreateScreenOrderInfo(orderId);
        List<OrderItemRow> items = orderQuery.getOrderItems(orderId);
        v.setOrderItems(items);
        return v;
    }

    @Transactional
    public long createReturn(long orderId, String reason, String note) {
        String code = "RET-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return repo.insertReturn(orderId,
                code,
                reason == null ? null : reason.trim(),
                note == null ? null : note.trim());
    }

    public ReturnIntakeView getIntakeView(long returnId) {
        ReturnIntakeView v = repo.getReturnHeader(returnId);
        v.setOrderItems(orderQuery.getOrderItems(v.getOrderId()));
        v.setScannedItems(repo.getReturnItems(returnId));
        return v;
    }

    @Transactional
    public void scanReturnedCopy(long returnId,
                                 long orderItemId,
                                 String copyCode,
                                 String conditionGrade,
                                 String conditionNote) {
        if (copyCode == null || copyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("copyCode is required");
        }
        if (conditionGrade == null || conditionGrade.trim().isEmpty()) {
            throw new IllegalArgumentException("conditionGrade is required");
        }

        String code = copyCode.trim();
        String grade = conditionGrade.trim().toUpperCase();

        var copyId = repo.findCopyIdByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy copyCode=" + code));

        // validate orderItem thuộc order của return
        var oi = repo.getOrderItemForReturn(returnId, orderItemId);

        // rule demo: nếu order_item có allocatedCopyId, scan phải đúng copy đó
        if (oi.allocatedCopyId() != null && oi.allocatedCopyId().longValue() != copyId) {
            throw new IllegalArgumentException("Copy scan không khớp copy đã bán cho orderItem này (allocatedCopyId=" + oi.allocatedCopyId() + ")");
        }

        // tránh scan trùng
        if (repo.existsReturnItemByCopy(returnId, copyId)) {
            throw new IllegalArgumentException("Copy này đã được scan vào return rồi.");
        }

        repo.insertReturnItem(
                returnId,
                orderItemId,
                copyId,
                grade,
                conditionNote == null ? null : conditionNote.trim()
        );
    }
}