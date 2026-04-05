package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.payos.PayOsPaymentSyncService;
import com.example.onlinebookshop.staff.dto.AllocatePreviewRow;
import com.example.onlinebookshop.staff.dto.OrderDetailView;
import com.example.onlinebookshop.staff.dto.OrderFilter;
import com.example.onlinebookshop.staff.dto.OrderListRow;
import com.example.onlinebookshop.staff.dto.ReturnScanMatchRow;
import com.example.onlinebookshop.staff.dto.StaffAlert;
import com.example.onlinebookshop.staff.repo.StaffAlertRepository;
import com.example.onlinebookshop.staff.repo.StaffOrderQueryRepository;
import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import com.example.onlinebookshop.staff.repo.StaffPackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class StaffOrderService {

    private final StaffOrderRepository staffOrderRepository;
    private final StaffOrderQueryRepository queryRepository;
    private final StaffAlertRepository alertRepository;
    private final StaffPackingRepository packingRepository;
    private final PayOsPaymentSyncService payOsPaymentSyncService;

    public StaffOrderService(StaffOrderRepository staffOrderRepository,
                             StaffOrderQueryRepository queryRepository,
                             StaffAlertRepository alertRepository,
                             StaffPackingRepository packingRepository,
                             PayOsPaymentSyncService payOsPaymentSyncService) {
        this.staffOrderRepository = staffOrderRepository;
        this.queryRepository = queryRepository;
        this.alertRepository = alertRepository;
        this.packingRepository = packingRepository;
        this.payOsPaymentSyncService = payOsPaymentSyncService;
    }

    public List<OrderListRow> getAll(OrderFilter filter) {
        List<OrderListRow> rows = queryRepository.findOrders(filter, 200);

        if (filter != null && "allocate".equalsIgnoreCase(filter.getStage())) {
            for (OrderListRow row : rows) {
                row.setPreviewItems(queryRepository.buildAllocatePreview(row.getId()));
            }
        }

        return rows;
    }

    public Order getById(Long id) {
        return staffOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<StaffAlert> getAlerts() {
        return alertRepository.getAlerts(20);
    }

    public List<Order> getTodoList() {
        return staffOrderRepository.findTodoTop10();
    }

    public OrderDetailView getDetail(long orderId) {
        return queryRepository.getOrderDetail(orderId);
    }

    @Transactional
    public void updateStaffNote(Long orderId, String staffNote) {
        Order order = getById(orderId);
        order.setStaffNote(staffNote == null ? null : staffNote.trim());
        staffOrderRepository.save(order);
    }

    @Transactional
    public void updateShipment(Long orderId, String carrier, String trackingCode) {
        Order order = getById(orderId);
        order.setCarrier(carrier == null ? null : carrier.trim());
        order.setTrackingCode(trackingCode == null ? null : trackingCode.trim());
        staffOrderRepository.save(order);
    }

    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = getById(orderId);
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new RuntimeException("paymentStatus is required");
        }
        String previous = order.getPaymentStatus();
        String next = paymentStatus.trim().toUpperCase();
        order.setPaymentStatus(next);
        staffOrderRepository.saveAndFlush(order);
        payOsPaymentSyncService.applyPayOsStockAfterManualPaymentUpdate(orderId, previous, next);
    }

    @Transactional
    public void updateStatus(Long id, String newStatus) {
        Order order = getById(id);
        applyStatus(order, requireStatus(newStatus));
        staffOrderRepository.save(order);
    }

    @Transactional
    public void bulkConfirm(List<Long> orderIds) {
        mutateInBulk(orderIds, "NEW", "CONFIRMED");
    }

    @Transactional
    public void bulkPack(List<Long> orderIds,
                         Map<Long, Integer> boxCounts,
                         Map<Long, String> packingNotes) {
        for (Long orderId : validateIds(orderIds)) {
            Order order = getById(orderId);
            String current = normalized(order.getStatus());

            if (!"CONFIRMED".equals(current)) {
                throw new RuntimeException("Chỉ pack được đơn CONFIRMED. Đơn lỗi: " + order.getOrderCode());
            }

            int packable = packingRepository.countPackableItems(orderId);
            int picked = packingRepository.countPicked(orderId);

            if (packable <= 0) {
                throw new RuntimeException("Đơn không có item hợp lệ để pack. Đơn lỗi: " + order.getOrderCode());
            }

            if (picked < packable) {
                throw new RuntimeException("Đơn chưa được pick đủ nên chưa pack được. Đơn lỗi: " + order.getOrderCode());
            }

            Integer boxCount = boxCounts == null ? null : boxCounts.get(orderId);
            String packingNote = packingNotes == null ? null : packingNotes.get(orderId);

            int resolvedBoxCount = (boxCount == null ? 1 : boxCount);
            if (resolvedBoxCount <= 0) {
                throw new RuntimeException("Số box phải > 0. Đơn lỗi: " + order.getOrderCode());
            }

            String appended = buildPackNoteLine(resolvedBoxCount, packingNote);
            String oldNote = order.getStaffNote();

            if (oldNote == null || oldNote.trim().isEmpty()) {
                order.setStaffNote(appended);
            } else {
                order.setStaffNote(oldNote.trim() + "\n" + appended);
            }

            applyStatus(order, "PACKED");
            staffOrderRepository.save(order);
        }
    }

    @Transactional
    public void bulkShip(List<Long> orderIds, String carrier) {
        if (carrier == null || carrier.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập carrier cho lô ship.");
        }
        for (Long orderId : validateIds(orderIds)) {
            Order order = getById(orderId);
            String current = normalized(order.getStatus());
            if (!"PACKED".equals(current)) {
                throw new RuntimeException("Chỉ ship được đơn PACKED. Đơn lỗi: " + order.getOrderCode());
            }
            order.setCarrier(carrier.trim());
            applyStatus(order, "SHIPPED");
            staffOrderRepository.save(order);
        }
    }

    @Transactional
    public void bulkDeliver(List<Long> orderIds) {
        mutateInBulk(orderIds, "SHIPPED", "DELIVERED");
    }

    @Transactional
    public void autoAllocate(Long orderId) {
        Order order = getById(orderId);
        String current = normalized(order.getStatus());
        if (!"CONFIRMED".equals(current)) {
            throw new RuntimeException("Chỉ allocate được đơn CONFIRMED.");
        }

        queryRepository.autoAllocateAllItems(orderId);
    }

    @Transactional
    public List<AllocatePreviewRow> getAllocatePreview(Long orderId) {
        Order order = getById(orderId);
        String current = normalized(order.getStatus());
        if (!"CONFIRMED".equals(current)) {
            throw new RuntimeException("Chỉ preview allocate cho đơn CONFIRMED.");
        }
        return queryRepository.buildAllocatePreview(orderId);
    }

    @Transactional
    public void confirmAutoAllocateAndPick(Long orderId) {
        Order order = getById(orderId);
        String current = normalized(order.getStatus());
        if (!"CONFIRMED".equals(current)) {
            throw new RuntimeException("Chỉ auto allocate/pick cho đơn CONFIRMED.");
        }

        int updated = queryRepository.confirmAutoAllocateAndPick(orderId);
        if (updated == 0) {
            throw new RuntimeException("Không có item nào được allocate/pick.");
        }
    }

    public StaffDashboardStats getDashboardStats() {
        try {
            long newOrders = staffOrderRepository.countNewOrders();
            long pendingPayments = staffOrderRepository.countPendingPayments();
            long toPack = staffOrderRepository.countToPack();

            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            long shippedToday = staffOrderRepository.countShippedBetween(start, end);

            LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(24);
            long overdue = staffOrderRepository.countOverdueBefore(overdueThreshold);

            return new StaffDashboardStats(newOrders, pendingPayments, toPack, shippedToday, overdue);
        } catch (Exception ex) {
            return new StaffDashboardStats(0, 0, 0, 0, 0);
        }
    }

    @Transactional
    public void createReturnIntakeMulti(Long orderId,
                                        String copyCodes,
                                        String reason,
                                        String receivedConditionGrade,
                                        String receivedConditionNote) {
        Order order = getById(orderId);
        String current = normalized(order.getStatus());

        if (!"DELIVERED".equals(current) && !"COMPLETED".equals(current)) {
            throw new RuntimeException("Chỉ tạo return intake cho đơn DELIVERED hoặc COMPLETED.");
        }

        if (copyCodes == null || copyCodes.trim().isEmpty()) {
            throw new RuntimeException("Phải nhập ít nhất 1 copyCode.");
        }

        String normalizedGrade = receivedConditionGrade == null ? null : receivedConditionGrade.trim().toUpperCase();
        if (normalizedGrade == null ||
                (!normalizedGrade.equals("NEW")
                        && !normalizedGrade.equals("LIKE_NEW")
                        && !normalizedGrade.equals("GOOD")
                        && !normalizedGrade.equals("FAIR"))) {
            throw new RuntimeException("Actual condition on return phải là NEW, LIKE_NEW, GOOD hoặc FAIR.");
        }

        Set<String> uniqueCodes = new LinkedHashSet<>();
        for (String raw : copyCodes.split("\\r?\\n")) {
            String code = raw == null ? "" : raw.trim();
            if (!code.isEmpty()) {
                uniqueCodes.add(code);
            }
        }

        if (uniqueCodes.isEmpty()) {
            throw new RuntimeException("Không có copyCode hợp lệ.");
        }

        ArrayList<ReturnScanMatchRow> matchedRows = new ArrayList<>();
        for (String code : uniqueCodes) {
            ReturnScanMatchRow matched = queryRepository.findReturnedCopyInOrder(orderId, code);
            if (matched == null) {
                throw new RuntimeException("copyCode không thuộc order này hoặc không tìm thấy cuốn đã bán: " + code);
            }
            if (queryRepository.existsReturnItemByCopyId(matched.getCopyId())) {
                throw new RuntimeException("Cuốn này đã được intake return trước đó: " + code);
            }
            matchedRows.add(matched);
        }

        Long returnId = queryRepository.createReturnHeader(
                orderId,
                reason == null ? null : reason.trim(),
                "Return intake by staff - " + matchedRows.size() + " copies"
        );

        for (ReturnScanMatchRow matched : matchedRows) {
            queryRepository.createReturnItem(
                    returnId,
                    matched.getOrderItemId(),
                    matched.getCopyId(),
                    normalizedGrade,
                    receivedConditionNote == null ? null : receivedConditionNote.trim(),
                    null
            );

            queryRepository.updateCopyAfterReturn(
                    matched.getCopyId(),
                    "RETURNED",
                    normalizedGrade,
                    receivedConditionNote == null ? "Returned - waiting manager decision" : receivedConditionNote.trim()
            );

            queryRepository.insertReturnInventoryTransaction(
                    matched.getVariantId(),
                    matched.getCopyId(),
                    returnId,
                    "Return intake for order " + matched.getOrderCode() + " / copyCode " + matched.getCopyCode()
            );
        }
    }

    private void mutateInBulk(List<Long> orderIds, String expectedCurrent, String targetStatus) {
        for (Long orderId : validateIds(orderIds)) {
            Order order = getById(orderId);
            String current = normalized(order.getStatus());
            if (!expectedCurrent.equals(current)) {
                throw new RuntimeException("Chỉ xử lý được đơn ở trạng thái " + expectedCurrent + ". Đơn lỗi: " + order.getOrderCode());
            }
            applyStatus(order, targetStatus);
            staffOrderRepository.save(order);
        }
    }

    private List<Long> validateIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn đơn nào.");
        }
        return orderIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String requireStatus(String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new RuntimeException("New status is required");
        }
        return newStatus.trim().toUpperCase();
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String buildPackNoteLine(int boxCount, String packingNote) {
        String safeNote = packingNote == null ? "" : packingNote.trim();
        return "[PACK] boxes=" + boxCount + " | " + safeNote + " | at=" + LocalDateTime.now();
    }

    private void applyStatus(Order order, String ns) {
        String current = normalized(order.getStatus());
        if ("COMPLETED".equals(current) || "CANCELLED".equals(current)) {
            throw new RuntimeException("Cannot modify final state order");
        }

        switch (ns) {
            case "NEW" -> { }
            case "CONFIRMED" -> {
                order.setStatus("CONFIRMED");
                if (order.getConfirmedAt() == null) {
                    order.setConfirmedAt(LocalDateTime.now());
                }
            }
            case "PACKED" -> {
                order.setStatus("PACKED");
                order.setPackedAt(LocalDateTime.now());
            }
            case "SHIPPED" -> {
                order.setStatus("SHIPPED");
                order.setShippedAt(LocalDateTime.now());
            }
            case "DELIVERED" -> {
                order.setStatus("DELIVERED");
                order.setDeliveredAt(LocalDateTime.now());
            }
            case "COMPLETED" -> {
                order.setStatus("COMPLETED");
                order.setCompletedAt(LocalDateTime.now());
            }
            case "CANCELLED" -> {
                order.setStatus("CANCELLED");
                order.setCancelledAt(LocalDateTime.now());
            }
            default -> throw new RuntimeException("Unsupported status: " + ns);
        }
    }
}