package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.ShippingItemRow;
import com.example.onlinebookshop.staff.repo.StaffShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.onlinebookshop.Entity.Order;

import java.util.List;

@Service
public class StaffShippingService {

    private final StaffShippingRepository repo;

    public StaffShippingService(StaffShippingRepository repo) {
        this.repo = repo;
    }

    public ShippingView getShippingView(long orderId) {
        var h = repo.getOrderHeader(orderId);
        ShippingView v = new ShippingView();
        v.orderId = h.id();
        v.orderCode = h.orderCode();
        v.status = h.status();
        v.paymentStatus = h.paymentStatus();

        v.shipName = h.shipName();
        v.shipPhone = h.shipPhone();
        v.shipLine1 = h.shipLine1();
        v.shipLine2 = h.shipLine2();
        v.shipWard = h.shipWard();
        v.shipDistrict = h.shipDistrict();
        v.shipCity = h.shipCity();
        v.shipProvince = h.shipProvince();

        v.carrier = h.carrier();
        v.trackingCode = h.trackingCode();

        v.totalItems = repo.countPackableItems(orderId);
        v.allocatedItems = repo.countAllocated(orderId);
        v.pickedItems = repo.countPicked(orderId);

        v.items = repo.getItemsForSlip(orderId);
        return v;
    }

    @Transactional
    public void confirmShipped(long orderId, String carrier, String trackingCode) {

        String c = carrier == null || carrier.trim().isEmpty() ? "Nhân viên giao hàng" : carrier.trim();
        String t = trackingCode == null || trackingCode.trim().isEmpty()
                ? "SELF-" + orderId + "-" + System.currentTimeMillis() % 100000
                : trackingCode.trim();

        var h = repo.getOrderHeader(orderId);

        // Rule: chỉ ship khi PACKED (hoặc đã SHIPPED để idempotent)
        if (!"PACKED".equalsIgnoreCase(h.status()) && !"SHIPPED".equalsIgnoreCase(h.status())) {
            throw new IllegalStateException("Order phải PACKED trước khi ship. status=" + h.status());
        }

        int r = repo.markShipped(orderId, c, t);
        if (r == 0) throw new IllegalStateException("Không update được SHIPPED (order có thể bị đổi trạng thái).");
    }

    public static class ShippingView {
        public long orderId;
        public String orderCode;
        public String status;
        public String paymentStatus;

        public int totalItems;
        public int allocatedItems;
        public int pickedItems;

        public String shipName;
        public String shipPhone;
        public String shipLine1;
        public String shipLine2;
        public String shipWard;
        public String shipDistrict;
        public String shipCity;
        public String shipProvince;

        public String carrier;
        public String trackingCode;

        public List<ShippingItemRow> items;
    }
}