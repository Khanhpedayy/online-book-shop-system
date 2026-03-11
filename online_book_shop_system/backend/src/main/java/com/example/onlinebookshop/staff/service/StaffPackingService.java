package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.ShippingItemRow;
import com.example.onlinebookshop.staff.repo.StaffPackingRepository;
import com.example.onlinebookshop.staff.repo.StaffShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffPackingService {

    private final StaffPackingRepository repo;
    private final StaffShippingRepository shippingRepo;

    public StaffPackingService(StaffPackingRepository repo, StaffShippingRepository shippingRepo) {
        this.repo = repo;
        this.shippingRepo = shippingRepo;
    }

    public PackingView getPackingView(long orderId) {
        var h = repo.getOrderHeader(orderId);

        int packable = repo.countPackableItems(orderId);
        int allocated = repo.countAllocated(orderId);
        int picked = repo.countPicked(orderId);

        PackingView v = new PackingView();
        v.orderId = h.id();
        v.orderCode = h.orderCode();
        v.status = h.status();
        v.paymentStatus = h.paymentStatus();
        v.packableItems = packable;
        v.allocatedItems = allocated;
        v.pickedItems = picked;

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

        v.items = shippingRepo.getItemsForSlip(orderId);
        return v;
    }

    @Transactional
    public void confirmPacked(long orderId, int boxCount, String packingNote) {
        if (boxCount <= 0) throw new IllegalArgumentException("boxCount phải > 0");

        var h = repo.getOrderHeader(orderId);

        int packable = repo.countPackableItems(orderId);
        int allocated = repo.countAllocated(orderId);
        int picked = repo.countPicked(orderId);

        if (!"CONFIRMED".equalsIgnoreCase(h.status()) && !"PACKED".equalsIgnoreCase(h.status())) {
            throw new IllegalStateException("Order phải ở trạng thái CONFIRMED (hoặc đã PACKED) mới pack được. status=" + h.status());
        }

        if (allocated < packable) {
            throw new IllegalStateException("Chưa allocate đủ copy cho order. allocated=" + allocated + ", packable=" + packable);
        }

        if (picked < allocated) {
            throw new IllegalStateException("Chưa pick đủ. picked=" + picked + ", allocated=" + allocated);
        }

        int r = repo.markPacked(orderId, boxCount, packingNote == null ? null : packingNote.trim());
        if (r == 0) throw new IllegalStateException("Không update được trạng thái PACKED (order có thể bị đổi trạng thái).");
    }

    public static class PackingView {
        public long orderId;
        public String orderCode;
        public String status;
        public String paymentStatus;

        public int packableItems;
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