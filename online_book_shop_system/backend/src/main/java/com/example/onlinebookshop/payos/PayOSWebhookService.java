package com.example.onlinebookshop.payos;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PayOSWebhookService {

    private final PayOsPaymentSyncService payOsPaymentSyncService;

    public PayOSWebhookService(PayOsPaymentSyncService payOsPaymentSyncService) {
        this.payOsPaymentSyncService = payOsPaymentSyncService;
    }

    public void applyPaymentResult(Map<String, Object> data, String paymentSignatureStatus) {
        if (data == null) {
            throw new IllegalArgumentException("Webhook data is missing");
        }

        // From payOS docs: data.paymentLinkId
        Object paymentLinkIdObj = data.get("paymentLinkId");
        String paymentLinkId = paymentLinkIdObj != null ? paymentLinkIdObj.toString() : null;
        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            throw new IllegalArgumentException("paymentLinkId is missing in webhook payload");
        }

        // Normalize to our order/payment statuses.
        String status = paymentSignatureStatus;
        if (status == null || status.isBlank()) {
            status = "UNPAID";
        }

        // PAID => orders.payment_status = PAID and payment_logs.status = PAID; then reduce book stock once
        payOsPaymentSyncService.syncPaymentStatusByPaymentLinkId(paymentLinkId, status);
    }
}

