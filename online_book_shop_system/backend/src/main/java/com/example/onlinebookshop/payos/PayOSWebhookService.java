package com.example.onlinebookshop.payos;

import com.example.onlinebookshop.wallet.CodDepositRepository;
import com.example.onlinebookshop.wallet.CodWalletService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PayOSWebhookService {

    private final PayOsPaymentSyncService payOsPaymentSyncService;
    private final CodDepositRepository codDepositRepository;
    private final CodWalletService codWalletService;

    public PayOSWebhookService(PayOsPaymentSyncService payOsPaymentSyncService,
                               CodDepositRepository codDepositRepository,
                               CodWalletService codWalletService) {
        this.payOsPaymentSyncService = payOsPaymentSyncService;
        this.codDepositRepository    = codDepositRepository;
        this.codWalletService        = codWalletService;
    }

    public void applyPaymentResult(Map<String, Object> data, String paymentSignatureStatus) {
        if (data == null) {
            throw new IllegalArgumentException("Webhook data is missing");
        }

        Object paymentLinkIdObj = data.get("paymentLinkId");
        String paymentLinkId = paymentLinkIdObj != null ? paymentLinkIdObj.toString() : null;
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            throw new IllegalArgumentException("paymentLinkId is missing in webhook payload");
        }

        String status = (paymentSignatureStatus == null || paymentSignatureStatus.isBlank())
                ? "UNPAID" : paymentSignatureStatus;

        // ─── Route: COD deposit hay customer order? ────────────────────
        if ("PAID".equals(status) && codDepositRepository.existsByPayosLinkId(paymentLinkId)) {
            // Shipper vừa nộp tiền COD qua PayOS
            codWalletService.handleDepositPaid(paymentLinkId);
        } else {
            // Luồng cũ: customer thanh toán đơn hàng
            payOsPaymentSyncService.syncPaymentStatusByPaymentLinkId(paymentLinkId, status);
        }
    }
}
