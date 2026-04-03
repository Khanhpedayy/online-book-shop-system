package com.example.onlinebookshop.stock;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository repo;

    public StockService(StockRepository repo) {
        this.repo = repo;
    }

    /* ═══ ĐỌC: tổng hợp từ lots ═══ */

    public List<StockItemDTO> getAllStock() {
        return repo.findAllStock();
    }

    public List<StockAdjustmentDTO> getAdjustments(Long bookId) {
        return repo.findAdjustments(bookId);
    }

    /* ═══ GHI: đã chuyển sang Lot-based (vô hiệu hoá chỉnh thủ công) ═══ */

    public StockItemDTO setStock(Long bookId, UpdateStockRequest req) {
        throw new UnsupportedOperationException(
            "Tồn kho được quản lý qua Lô hàng (Lots). " +
            "Dùng module Nhập kho để thêm hàng, hoặc Kiểm kê (Stocktaking) để điều chỉnh số lượng.");
    }

    public StockItemDTO adjustStock(Long bookId, AdjustStockRequest req) {
        throw new UnsupportedOperationException(
            "Tồn kho được quản lý qua Lô hàng (Lots). " +
            "Dùng module Nhập kho để thêm hàng, hoặc Kiểm kê (Stocktaking) để điều chỉnh số lượng.");
    }
}
