package com.example.onlinebookshop.paymentlog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class ManagerPaymentLogService {
    private final ManagerPaymentLogRepository repo;

    public ManagerPaymentLogService(ManagerPaymentLogRepository repo) {
        this.repo = repo;
    }

    public List<PaymentLogDTO> getByOrderId(Long orderId) {
        return repo.findByOrderId(orderId);
    }

    @Transactional
    public void flagPayment(Long id, FlagPaymentRequest req) {
        int u = repo.flagPayment(id, req.getReason());
        if (u == 0)
            throw new RuntimeException("Payment log not found: " + id);
    }

    public Map<String, String> recheck(Long id) {
        String status = repo.recheck(id);
        if (status == null)
            throw new RuntimeException("Payment log not found: " + id);
        return Map.of("status", status);
    }
}

