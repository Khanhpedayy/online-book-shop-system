package com.example.onlinebookshop.payos;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Verify payOS webhook signature.
 *
 * Signature data is built from payload.data object:
 * - sort keys alphabetically
 * - build string: key1=value1&key2=value2...
 * - null/undefined values become empty string
 * - HMAC_SHA256(dataString, checksumKey) => hex
 */
@Component
public class PayOSWebhookSignature {

    public boolean verify(Map<String, Object> data, String receivedSignature, String checksumKey) {
        if (data == null) return false;
        if (receivedSignature == null || receivedSignature.isBlank()) return false;
        if (checksumKey == null || checksumKey.isBlank()) return false;

        String computed = sign(data, checksumKey);
        return computed.equalsIgnoreCase(receivedSignature.trim());
    }

    private String sign(Map<String, Object> data, String checksumKey) {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Object rawValue = data.get(k);

            String v = valueToString(rawValue);

            if (i > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        }

        return hmacSha256Hex(sb.toString(), checksumKey);
    }

    private static String valueToString(Object rawValue) {
        if (rawValue == null) {
            return "";
        }
        if (rawValue instanceof Number n) {
            // Avoid double -> binary float artifacts by using BigDecimal string representation.
            try {
                return new java.math.BigDecimal(n.toString()).stripTrailingZeros().toPlainString();
            } catch (Exception e) {
                return n.toString();
            }
        }
        return String.valueOf(rawValue);
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
            throw new IllegalStateException("Failed to compute webhook signature", e);
        }
    }
}

