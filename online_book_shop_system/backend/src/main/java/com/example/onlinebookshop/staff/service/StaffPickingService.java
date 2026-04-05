package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffPickingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaffPickingService {

    private static final int RESERVE_TTL_MIN = 30;

    private final StaffPickingRepository repo;

    public StaffPickingService(StaffPickingRepository repo) {
        this.repo = repo;
    }

    public PickUiData buildPickUi(long orderId) {
        List<StaffPickingRepository.OrderItemInfo> items = repo.findOrderItemsByOrderId(orderId);

        Map<Long, List<StaffPickingRepository.CopyChoice>> availableCopiesByItemId = new LinkedHashMap<>();
        Map<Long, List<StaffPickingRepository.LotChoice>> availableLotsByItemId = new LinkedHashMap<>();
        Map<Long, StaffPickingRepository.BoundCopyDisplay> boundCopiesByItemId = new LinkedHashMap<>();

        for (StaffPickingRepository.OrderItemInfo item : items) {
            availableCopiesByItemId.put(item.id(), repo.findAvailableCopiesByOrderItemId(item.id()));
            availableLotsByItemId.put(item.id(), repo.findAvailableLotsByOrderItemId(item.id()));

            if (item.copyId() != null) {
                repo.findBoundCopyDisplayByCopyId(item.copyId())
                        .ifPresent(bound -> boundCopiesByItemId.put(item.id(), bound));
            }
        }

        return new PickUiData(availableCopiesByItemId, availableLotsByItemId, boundCopiesByItemId);
    }

    @Transactional
    public int splitOrderItemToSingleUnits(long orderId, long orderItemId) {
        if (!repo.canSplitOrderItem(orderId, orderItemId)) {
            throw new RuntimeException("Chỉ được tách dòng chưa bind copy, chưa pick và quantity > 1.");
        }

        int affected = repo.splitOrderItemToSingleUnits(orderId, orderItemId);
        if (affected <= 1) {
            throw new RuntimeException("Không tách được dòng order item.");
        }

        return affected;
    }

    @Transactional
    public AllocationResult autoAllocate(long orderId) {
        List<Long> itemIds = repo.findUnallocatedOrderItemIds(orderId);

        int allocated = 0;
        List<String> warnings = new ArrayList<>();

        for (Long itemId : itemIds) {
            long variantId = repo.getVariantIdByOrderItemId(itemId);

            var copyOpt = repo.findFifoAvailableCopyId(variantId);
            if (copyOpt.isEmpty()) {
                warnings.add("Không còn copy AVAILABLE cho variantId=" + variantId + " (orderItemId=" + itemId + ")");
                continue;
            }

            long copyId = copyOpt.get();

            int reserveResult = repo.reserveCopy(copyId, RESERVE_TTL_MIN);
            if (reserveResult == 0) {
                warnings.add("Copy vừa bị người khác reserve mất (copyId=" + copyId + ")");
                continue;
            }

            int lotReserve = repo.reserveLotCountersByCopy(copyId);
            if (lotReserve == 0) {
                repo.releaseCopy(copyId);
                warnings.add("Không cập nhật được lot reserve cho copyId=" + copyId);
                continue;
            }

            int assignResult = repo.assignCopyToOrderItem(itemId, copyId, "AUTO");
            if (assignResult == 0) {
                repo.releaseLotCountersByCopy(copyId);
                repo.releaseCopy(copyId);
                warnings.add("Không assign được copy cho orderItemId=" + itemId);
                continue;
            }

            var copyInfo = repo.findAvailableCopyById(copyId).orElse(null);
            if (copyInfo != null) {
                repo.insertInventoryTxReserve(variantId, copyInfo.lotId(), copyId, orderId, "AUTO allocate by staff");
            }

            allocated++;
        }

        return new AllocationResult(allocated, warnings);
    }

    @Transactional
    public void chooseAvailableCopyAndPick(long orderId, long orderItemId, long copyId) {
        StaffPickingRepository.OrderItemInfo item = repo.findOrderItemInfo(orderId, orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order item trong đơn."));

        if (item.quantity() != 1) {
            throw new RuntimeException("Dòng order_item có quantity > 1. Hãy tách dòng thành từng cuốn trước khi pick.");
        }

        if (item.copyId() != null) {
            throw new RuntimeException("Dòng này đã bind copy_id. Hãy dùng nút xác nhận lấy.");
        }

        StaffPickingRepository.CopyInfo copy = repo.findAvailableCopyById(copyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy copy khả dụng để chọn."));

        if (copy.variantId() != item.variantId()) {
            throw new RuntimeException("Copy được chọn không đúng SKU/variant của dòng hàng.");
        }

        if (!"AVAILABLE".equalsIgnoreCase(copy.status())) {
            throw new RuntimeException("Copy được chọn hiện không AVAILABLE.");
        }

        int reserveResult = repo.reserveCopy(copy.id(), RESERVE_TTL_MIN);
        if (reserveResult == 0) {
            throw new RuntimeException("Không reserve được copy đã chọn.");
        }

        int lotReserve = repo.reserveLotCountersByCopy(copy.id());
        if (lotReserve == 0) {
            repo.releaseCopy(copy.id());
            throw new RuntimeException("Không cập nhật được số lượng lot khi reserve.");
        }

        int assignResult = repo.assignCopyToOrderItem(orderItemId, copy.id(), "MANUAL");
        if (assignResult == 0) {
            repo.releaseLotCountersByCopy(copy.id());
            repo.releaseCopy(copy.id());
            throw new RuntimeException("Không gán được copy vào order item.");
        }

        repo.insertInventoryTxReserve(item.variantId(), copy.lotId(), copy.id(), orderId, "MANUAL choose available copy");

        int pickedCopy = repo.markCopyPicked(copy.id());
        if (pickedCopy == 0) {
            throw new RuntimeException("Không chuyển được trạng thái copy sang PICKED.");
        }

        repo.markOrderItemPicked(orderItemId, "MANUAL");
    }

    @Transactional
    public void createCopyFromLotAndPick(long orderId, long orderItemId, long lotId, String copyCode) {
        String normalizedCode = normalize(copyCode);
        if (normalizedCode == null) {
            throw new RuntimeException("Phải nhập copy code.");
        }

        StaffPickingRepository.OrderItemInfo item = repo.findOrderItemInfo(orderId, orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order item trong đơn."));

        if (item.quantity() != 1) {
            throw new RuntimeException("Dòng order_item có quantity > 1. Hãy tách dòng thành từng cuốn trước khi pick.");
        }

        if (item.copyId() != null) {
            throw new RuntimeException("Dòng này đã bind copy_id. Hãy dùng nút xác nhận lấy.");
        }

        if (repo.findCopyByCode(normalizedCode).isPresent()) {
            throw new RuntimeException("copy_code đã tồn tại trong hệ thống. Hãy nhập mã khác.");
        }

        StaffPickingRepository.LotInfo lot = repo.findAvailableLotByIdForOrderItem(orderItemId, lotId)
                .orElseThrow(() -> new RuntimeException("Lot không hợp lệ cho dòng hàng này."));

        if (lot.variantId() != item.variantId()) {
            throw new RuntimeException("Lot được chọn không đúng SKU/variant.");
        }

        if (lot.qtyAvailable() <= 0) {
            throw new RuntimeException("Lot đã hết qty_available.");
        }

        int lotReserve = repo.reserveLotCountersByLot(lotId);
        if (lotReserve == 0) {
            throw new RuntimeException("Không reserve được số lượng từ lot.");
        }

        Long newCopyId = repo.createReservedCopyFromLot(lotId, item.variantId(), normalizedCode);
        if (newCopyId == null) {
            throw new RuntimeException("Không tạo được copy mới từ lot.");
        }

        int assignResult = repo.assignCopyToOrderItem(orderItemId, newCopyId, "SCAN");
        if (assignResult == 0) {
            repo.releaseLotCountersByCopy(newCopyId);
            repo.releaseCopy(newCopyId);
            throw new RuntimeException("Không gán được copy mới vào order item.");
        }

        repo.insertInventoryTxReserve(item.variantId(), lotId, newCopyId, orderId, "SCAN create copy from lot and pick");

        int pickedCopy = repo.markCopyPicked(newCopyId);
        if (pickedCopy == 0) {
            throw new RuntimeException("Không chuyển được copy mới sang PICKED.");
        }

        repo.markOrderItemPicked(orderItemId, "SCAN");
    }

    @Transactional
    public void confirmAllocatedPick(long orderId, long orderItemId) {
        StaffPickingRepository.OrderItemInfo item = repo.findOrderItemInfo(orderId, orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order item trong đơn."));

        if (item.quantity() != 1) {
            throw new RuntimeException("Dòng order_item có quantity > 1. Hãy tách dòng thành từng cuốn trước khi pick.");
        }

        if (item.copyId() == null) {
            throw new RuntimeException("Dòng này chưa có copy_id. Hãy chọn copy hoặc tạo copy từ lot trước.");
        }

        int pickedCopy = repo.markCopyPicked(item.copyId());
        if (pickedCopy == 0) {
            throw new RuntimeException("Không chuyển được trạng thái copy sang PICKED.");
        }

        repo.markOrderItemPicked(orderItemId, "ALLOCATED_CONFIRM");
    }

    @Transactional
    public void unpick(long orderItemId) {
        StaffPickingRepository.OrderItemInfo item = repo.findOrderItemInfoByItemId(orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order item."));

        if (item.copyId() == null) {
            throw new RuntimeException("Dòng này chưa có copy để bỏ pick.");
        }

        repo.clearPickAndAssignment(orderItemId);
        repo.releaseCopy(item.copyId());
        repo.releaseLotCountersByCopy(item.copyId());
    }

    private String normalize(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    public record AllocationResult(int allocatedCount, List<String> warnings) {}

    public record PickUiData(
            Map<Long, List<StaffPickingRepository.CopyChoice>> availableCopiesByItemId,
            Map<Long, List<StaffPickingRepository.LotChoice>> availableLotsByItemId,
            Map<Long, StaffPickingRepository.BoundCopyDisplay> boundCopiesByItemId
    ) {}
}