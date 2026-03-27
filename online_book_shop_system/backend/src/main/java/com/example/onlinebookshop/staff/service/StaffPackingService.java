package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.dto.ShippingItemRow;
import com.example.onlinebookshop.staff.repo.StaffPackingRepository;
import com.example.onlinebookshop.staff.repo.StaffShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StaffPackingService {

    private static final Pattern PACK_LINE_PATTERN =
            Pattern.compile("\\[PACK\\]\\s*boxes=(\\d+)\\s*\\|\\s*(.*?)\\s*\\|\\s*at=.*", Pattern.CASE_INSENSITIVE);

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

        PackMeta meta = extractLatestPackMeta(h.staffNote());

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

        v.boxCount = meta.boxCount();
        v.packingNote = meta.packingNote();

        List<ShippingItemRow> items = shippingRepo.getItemsForSlip(orderId);

        boolean hasRealFulfillmentItems = items != null
                && !items.isEmpty()
                && items.stream().anyMatch(it ->
                (it.getCopyCode() != null && !it.getCopyCode().isBlank())
                        || (it.getLocation() != null && !it.getLocation().isBlank())
        );

        if (!hasRealFulfillmentItems) {
            items = shippingRepo.getFallbackItemsForSlip(orderId);
        }

        v.items = items;
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

    private PackMeta extractLatestPackMeta(String staffNote) {
        if (staffNote == null || staffNote.isBlank()) {
            return new PackMeta(1, "");
        }

        String[] lines = staffNote.split("\\r?\\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            Matcher matcher = PACK_LINE_PATTERN.matcher(lines[i].trim());
            if (matcher.matches()) {
                int boxCount = Integer.parseInt(matcher.group(1));
                String packingNote = matcher.group(2) == null ? "" : matcher.group(2).trim();
                return new PackMeta(boxCount, packingNote);
            }
        }

        return new PackMeta(1, "");
    }

    private record PackMeta(int boxCount, String packingNote) {
    }

    public List<PackingView> getPackingViews(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn đơn nào.");
        }

        List<PackingView> views = new java.util.ArrayList<>();
        java.util.Set<Long> uniqueIds = new java.util.LinkedHashSet<>(orderIds);

        for (Long orderId : uniqueIds) {
            if (orderId != null) {
                views.add(getPackingView(orderId));
            }
        }

        if (views.isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn đơn nào.");
        }

        return views;
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

        public int boxCount;
        public String packingNote;

        public List<ShippingItemRow> items;
    }
}