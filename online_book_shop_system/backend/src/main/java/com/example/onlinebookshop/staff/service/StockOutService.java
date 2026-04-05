package com.example.onlinebookshop.staff.service;

import com.example.onlinebookshop.staff.repo.StaffOrderRepository;
import com.example.onlinebookshop.staff.repo.StockOutItemRepository;
import com.example.onlinebookshop.staff.repo.StockOutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StockOutService {

    private final StaffOrderRepository orderRepo;
    private final StockOutRepository stockOutRepo;
    private final StockOutItemRepository itemRepo;

    public StockOutService(StaffOrderRepository orderRepo,
                           StockOutRepository stockOutRepo,
                           StockOutItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.stockOutRepo = stockOutRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional
    public Long createStockOut(Long orderId, Long userId) {
        if (orderRepo.countConfirmedById(orderId) == 0) {
            throw new RuntimeException("Order chưa ở trạng thái CONFIRMED.");
        }

        if (stockOutRepo.existsActive(orderId)) {
            throw new RuntimeException("Đơn này đã có phiếu xuất active.");
        }

        if (itemRepo.countUnallocatedItems(orderId) > 0) {
            throw new RuntimeException("Còn item chưa bind copy_id. Staff phải allocate hoặc scan copy trước khi tạo phiếu xuất.");
        }

        Long stockOutId = stockOutRepo.insert(orderId, userId);
        itemRepo.insertFromOrder(orderId, stockOutId);

        if (itemRepo.countItemsByStockOutId(stockOutId) == 0) {
            throw new RuntimeException("Không tạo được dòng phiếu xuất. Kiểm tra order_items.copy_id.");
        }

        return stockOutId;
    }

    public Optional<StockOutRepository.StockOutSummary> getActiveStockOut(Long orderId) {
        return stockOutRepo.findActiveSummaryByOrderId(orderId);
    }

    public PickPageView getPickPage(Long stockOutId) {
        stockOutRepo.markPrintedIfCreated(stockOutId);

        StockOutRepository.StockOutSummary stockOut = stockOutRepo.findSummaryById(stockOutId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất."));

        List<StockOutItemRepository.PickItemRow> items = itemRepo.findPickRows(stockOutId);

        long totalItems = items.size();
        long pickedItems = items.stream().filter(i -> i.getPickedAt() != null).count();
        long missingItems = items.stream().filter(StockOutItemRepository.PickItemRow::isMissingReported).count();

        return new PickPageView(stockOut, items, totalItems, pickedItems, missingItems);
    }

    @Transactional
    public void pickItem(Long stockOutId, Long itemId, Long userId) {
        if (!itemRepo.itemBelongsToStockOut(stockOutId, itemId)) {
            throw new RuntimeException("Item không thuộc phiếu xuất này.");
        }

        stockOutRepo.markPrintedIfCreated(stockOutId);
        stockOutRepo.markPickingIfNeeded(stockOutId);

        itemRepo.markPicked(itemId, userId);
        itemRepo.updateCopyStatusPicked(itemId);
        itemRepo.updateOrderItemPicked(itemId, userId);

        stockOutRepo.markPickedIfReady(stockOutId, userId);
    }

    @Transactional
    public void reportMissing(Long stockOutId, Long itemId, String note) {
        if (!itemRepo.itemBelongsToStockOut(stockOutId, itemId)) {
            throw new RuntimeException("Item không thuộc phiếu xuất này.");
        }

        String resolvedNote = (note == null || note.isBlank())
                ? "Không tìm thấy sách tại vị trí kho"
                : note.trim();

        stockOutRepo.markPrintedIfCreated(stockOutId);
        stockOutRepo.markPickingIfNeeded(stockOutId);

        itemRepo.markMissing(itemId, resolvedNote);
        stockOutRepo.markException(stockOutId, "inventory mismatch during picking");
    }

    public static class PickPageView {
        private final StockOutRepository.StockOutSummary stockOut;
        private final List<StockOutItemRepository.PickItemRow> items;
        private final long totalItems;
        private final long pickedItems;
        private final long missingItems;

        public PickPageView(StockOutRepository.StockOutSummary stockOut,
                            List<StockOutItemRepository.PickItemRow> items,
                            long totalItems,
                            long pickedItems,
                            long missingItems) {
            this.stockOut = stockOut;
            this.items = items;
            this.totalItems = totalItems;
            this.pickedItems = pickedItems;
            this.missingItems = missingItems;
        }

        public StockOutRepository.StockOutSummary getStockOut() {
            return stockOut;
        }

        public List<StockOutItemRepository.PickItemRow> getItems() {
            return items;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public long getPickedItems() {
            return pickedItems;
        }

        public long getMissingItems() {
            return missingItems;
        }
    }
}