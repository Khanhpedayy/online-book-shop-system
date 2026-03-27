package com.example.onlinebookshop.payos;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Request signature for PayOS create payment link:
 * {@code amount=&cancelUrl=&description=&orderCode=&returnUrl=} (sorted keys, values interpolated as strings).
 */
public final class PayOSSignature {

    private PayOSSignature() {
    }

    public static String signCreateLink(
            long amount,
            String cancelUrl,
            String description,
            int orderCode,
            String returnUrl,
            String checksumKey
    ) {
        String data = "amount=" + amount
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrl;
        return hmacSha256Hex(data, checksumKey);
    }

    private static String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute PayOS signature", e);
        }
    }
}
