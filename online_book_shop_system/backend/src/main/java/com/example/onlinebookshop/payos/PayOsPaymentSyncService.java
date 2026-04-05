package com.example.onlinebookshop.payos;

import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Entity.OrderItem;
import com.example.onlinebookshop.Repository.OrderRepository;
import com.example.onlinebookshop.stock.StockRepository;
import com.example.onlinebookshop.paymentlog.PayOsAffectedOrderRow;
import com.example.onlinebookshop.paymentlog.PaymentLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies PayOS payment status to DB and, when an order first becomes PAID via PayOS,
 * decrements {@code lots.qty_available} by line quantities (grouped per book), then
 * refreshes {@code books.stock_quantity} from lots (same model as cart / checkout validation).
 */
@Service
public class PayOsPaymentSyncService {

    private final PaymentLogRepository paymentLogRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    public PayOsPaymentSyncService(PaymentLogRepository paymentLogRepository,
                                   OrderRepository orderRepository,
                                   StockRepository stockRepository) {
        this.paymentLogRepository = paymentLogRepository;
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void syncPaymentStatusByPaymentLinkId(String paymentLinkId, String status) {
        List<PayOsAffectedOrderRow> before = paymentLogRepository.findOrdersLinkedToPayosPaymentLink(paymentLinkId);
        String normalized = status != null ? status.trim().toUpperCase() : "";
        paymentLogRepository.syncPaymentStatusByPaymentLinkId(paymentLinkId, normalized);

        if (!"PAID".equals(normalized)) {
            return;
        }
        for (PayOsAffectedOrderRow row : before) {
            if (row.paymentMethod() == null || !"PAYOS".equalsIgnoreCase(row.paymentMethod())) {
                continue;
            }
            if (row.previousPaymentStatus() != null && "PAID".equalsIgnoreCase(row.previousPaymentStatus())) {
                continue;
            }
            deductStockForPaidPayOsOrder(row.orderId());
        }
    }

    /**
     * Staff (recheck) or any code path that sets {@code orders.payment_status} to PAID without going through PayOS sync.
     */
    @Transactional
    public void applyPayOsStockAfterManualPaymentUpdate(Long orderId, String previousPaymentStatus, String newPaymentStatus) {
        if (!"PAID".equalsIgnoreCase(newPaymentStatus != null ? newPaymentStatus.trim() : "")) {
            return;
        }
        if (previousPaymentStatus != null && "PAID".equalsIgnoreCase(previousPaymentStatus.trim())) {
            return;
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getPaymentMethod() == null || !"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
            return;
        }
        deductStockForPaidPayOsOrder(orderId);
    }

    private void deductStockForPaidPayOsOrder(long orderId) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        if (!"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
            return;
        }

        Map<Long, Integer> qtyByBookId = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() == null || item.getVariant().getBook() == null) {
                continue;
            }
            Long bookId = item.getVariant().getBook().getId();
            int q = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
            qtyByBookId.merge(bookId, q, Integer::sum);
        }

        for (Map.Entry<Long, Integer> e : qtyByBookId.entrySet()) {
            int need = e.getValue();
            int deducted = stockRepository.decrementAvailableForBook(e.getKey(), need);
            if (deducted != need) {
                throw new IllegalStateException(
                        "Không trừ được tồn kho cho sách id=" + e.getKey()
                                + " (cần " + need + ", trừ được " + deducted + "). Có thể tồn kho không đủ hoặc đã bị trừ.");
            }
            stockRepository.refreshBookStockQuantityFromLots(e.getKey());
        }
    }
}
