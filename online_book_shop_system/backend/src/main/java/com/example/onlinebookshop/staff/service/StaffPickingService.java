package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffPickingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service cho Allocation/Picking.
 * - autoAllocate: FIFO theo copies.created_at
 * - pickByScan: scan copyCode để gán cho 1 orderItem
 * - unpick: bỏ gán, trả copy về AVAILABLE
 */
@Service
public class StaffPickingService {

    private final StaffPickingRepository repo;

    // demo: giữ hàng 30 phút
    private static final int RESERVE_TTL_MIN = 30;

    public StaffPickingService(StaffPickingRepository repo) {
        this.repo = repo;
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

            int r1 = repo.reserveCopy(copyId, RESERVE_TTL_MIN);
            if (r1 == 0) {
                warnings.add("Copy vừa bị người khác lấy mất (copyId=" + copyId + ")");
                continue;
            }

            int r2 = repo.assignCopyToOrderItem(itemId, copyId, "AUTO");
            if (r2 == 0) {
                // rollback hành động reserve để tránh giữ hàng "mồ côi"
                repo.releaseCopy(copyId);
                warnings.add("Không assign được copy cho orderItemId=" + itemId);
                continue;
            }

            repo.insertInventoryTxReserve(variantId, copyId, orderId, "AUTO allocate by staff");
            allocated++;
        }

        return new AllocationResult(allocated, warnings);
    }

    @Transactional
    public void pickByScan(long orderId, long orderItemId, String copyCode) {
        if (copyCode == null || copyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("copyCode is required");
        }
        String code = copyCode.trim();

        long expectedVariantId = repo.getVariantIdByOrderItemId(orderItemId);

        var copy = repo.findCopyByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy copyCode=" + code));

        if (copy.variantId() != expectedVariantId) {
            throw new IllegalArgumentException("Copy không khớp variant. expected=" + expectedVariantId + ", actual=" + copy.variantId());
        }

        if (!"AVAILABLE".equalsIgnoreCase(copy.status())) {
            throw new IllegalArgumentException("Copy không AVAILABLE (status=" + copy.status() + ")");
        }

        int r1 = repo.reserveCopy(copy.id(), RESERVE_TTL_MIN);
        if (r1 == 0) throw new IllegalStateException("Không reserve được copy (có thể vừa bị giữ bởi người khác)");

        int r2 = repo.assignCopyToOrderItem(orderItemId, copy.id(), "SCAN");
        if (r2 == 0) {
            repo.releaseCopy(copy.id());
            throw new IllegalStateException("Order item đã được allocate trước đó.");
        }

        repo.insertInventoryTxReserve(expectedVariantId, copy.id(), orderId, "MANUAL scan by staff");
    }

    @Transactional
    public void unpick(long orderItemId) {
        var copyIdOpt = repo.getAssignedCopyId(orderItemId);
        if (copyIdOpt.isEmpty()) return; // nothing to do

        long copyId = copyIdOpt.get();

        repo.unassignCopyFromOrderItem(orderItemId);
        repo.releaseCopy(copyId);
    }

    public record AllocationResult(int allocatedCount, List<String> warnings) {}
}