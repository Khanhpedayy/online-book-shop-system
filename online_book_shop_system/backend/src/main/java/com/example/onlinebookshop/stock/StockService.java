package com.example.onlinebookshop.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository repo;

    public StockService(StockRepository repo) {
        this.repo = repo;
    }

    public List<StockItemDTO> getAllStock() {
        return repo.findAllStock();
    }

    public List<StockAdjustmentDTO> getAdjustments(Long bookId) {
        return repo.findAdjustments(bookId);
    }

    /* ═══ SET STOCK (direct update) ═══ */

    @Transactional
    public StockItemDTO setStock(Long bookId, UpdateStockRequest req) {
        if (!repo.bookExists(bookId))
            throw new RuntimeException("Sách không tồn tại: " + bookId);
        if (req.getQuantity() < 0)
            throw new IllegalArgumentException("Số lượng tồn kho không thể âm");

        int oldQty = repo.getStockQuantity(bookId);
        int newQty = req.getQuantity();
        int delta = newQty - oldQty;

        repo.setStockQuantity(bookId, newQty);
        repo.insertAdjustment(bookId, "SET", delta, oldQty, newQty,
                "Cập nhật trực tiếp", req.getNote());

        return getAllStock().stream()
                .filter(s -> s.getBookId().equals(bookId))
                .findFirst().orElse(null);
    }

    /* ═══ ADJUST STOCK (import/export) ═══ */

    @Transactional
    public StockItemDTO adjustStock(Long bookId, AdjustStockRequest req) {
        if (!repo.bookExists(bookId))
            throw new RuntimeException("Sách không tồn tại: " + bookId);
        if (req.getQuantity() <= 0)
            throw new IllegalArgumentException("Số lượng điều chỉnh phải lớn hơn 0");
        if (req.getType() == null || req.getType().isBlank())
            throw new IllegalArgumentException("Loại điều chỉnh là bắt buộc");

        int oldQty = repo.getStockQuantity(bookId);
        int delta;

        switch (req.getType().toUpperCase()) {
            case "IMPORT":
            case "RETURN":
                delta = req.getQuantity();
                break;
            case "EXPORT":
            case "DAMAGE":
                delta = -req.getQuantity();
                break;
            default:
                throw new IllegalArgumentException("Loại điều chỉnh không hợp lệ: " + req.getType());
        }

        int newQty = oldQty + delta;
        if (newQty < 0)
            throw new IllegalArgumentException(
                    "Không thể xuất " + req.getQuantity() + " cuốn. Tồn kho hiện tại: " + oldQty);

        repo.setStockQuantity(bookId, newQty);
        repo.insertAdjustment(bookId, req.getType().toUpperCase(), delta, oldQty, newQty,
                req.getReason(), req.getNote());

        return getAllStock().stream()
                .filter(s -> s.getBookId().equals(bookId))
                .findFirst().orElse(null);
    }
}
