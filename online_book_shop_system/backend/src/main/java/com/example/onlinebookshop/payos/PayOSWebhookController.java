package com.example.onlinebookshop.payos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payos")
public class PayOSWebhookController {

    private final PayOSProperties properties;
    private final PayOSWebhookSignature signatureVerifier;
    private final PayOSWebhookService service;

    public PayOSWebhookController(PayOSProperties properties,
                                  PayOSWebhookSignature signatureVerifier,
                                  PayOSWebhookService service) {
        this.properties = properties;
        this.signatureVerifier = signatureVerifier;
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        Object sigObj = payload.get("signature");
        String signature = sigObj != null ? sigObj.toString() : null;

        Object dataObj = payload.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = dataObj instanceof Map ? (Map<String, Object>) dataObj : null;

        if (data == null || signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Verify signature against checksumKey.
        boolean ok = signatureVerifier.verify(data, signature, properties.getChecksumKey());
        System.out.println("[PayOS webhook] signatureOk=" + ok + ", paymentLinkId=" + data.get("paymentLinkId"));
        if (!ok) {
            return ResponseEntity.status(401).build();
        }

        String rootCode = payload.get("code") != null ? payload.get("code").toString() : null;
        Object successObj = payload.get("success");
        boolean success;
        if (successObj instanceof Boolean b) {
            success = b;
        } else {
            success = successObj != null && Boolean.parseBoolean(successObj.toString());
        }

        String dataCode = data.get("code") != null ? data.get("code").toString() : null;

        boolean paid = "00".equalsIgnoreCase(rootCode)
                && success
                && "00".equalsIgnoreCase(dataCode);
        System.out.println("[PayOS webhook] rootCode=" + rootCode + ", success=" + success + ", data.code=" + dataCode + ", paid=" + paid);

        String targetStatus = paid ? "PAID" : "UNPAID";
        service.applyPaymentResult(data, targetStatus);

        return ResponseEntity.ok().build();
    }
}

