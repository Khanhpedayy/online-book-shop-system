package com.example.onlinebookshop.payos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PayOSClient {

    private static final String CREATE_PATH = "/v2/payment-requests";

    private final PayOSProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PayOSClient(PayOSProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a PayOS payment link. Amount is the order total rounded to whole currency units (VND-style integer).
     */
    public PayOSCheckoutResult createCheckout(long orderId, BigDecimal totalAmount, String orderCodeLabel) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "PayOS is disabled. Set payos.enabled=true and configure payos.client-id, payos.api-key, payos.checksum-key.");
        }
        if (!properties.hasCredentials()) {
            throw new IllegalStateException("PayOS credentials are missing (client-id, api-key, checksum-key).");
        }

        int payosOrderCode = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        long amount = totalAmount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive for PayOS");
        }

        String returnUrl = properties.getReturnUrlTemplate().replace("{orderId}", Long.toString(orderId));
        String cancelUrl = properties.getCancelUrlTemplate().replace("{orderId}", Long.toString(orderId));
        String description = buildDescription(orderCodeLabel, orderId);

        String signature = PayOSSignature.signCreateLink(
                amount,
                cancelUrl,
                description,
                payosOrderCode,
                returnUrl,
                properties.getChecksumKey()
        );

        ObjectNode body = objectMapper.createObjectNode();
        body.put("orderCode", payosOrderCode);
        body.put("amount", amount);
        body.put("description", description);
        body.put("cancelUrl", cancelUrl);
        body.put("returnUrl", returnUrl);
        body.put("signature", signature);

        String url = properties.getBaseUrl().replaceAll("/+$", "") + CREATE_PATH;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", properties.getClientId().trim());
        headers.set("x-api-key", properties.getApiKey().trim());

        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize PayOS request", e);
        }

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException(
                    "PayOS HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Invalid PayOS response: " + response.getBody(), e);
        }

        String code = root.path("code").asText("");
        if (!"00".equals(code)) {
            String desc = root.path("desc").asText("PayOS error");
            throw new IllegalStateException("PayOS create link failed: " + desc);
        }

        JsonNode data = root.path("data");
        String checkoutUrl = data.path("checkoutUrl").asText(null);
        String paymentLinkId = data.path("paymentLinkId").asText(null);
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException("PayOS response missing checkoutUrl");
        }

        return new PayOSCheckoutResult(payosOrderCode, paymentLinkId, checkoutUrl, amount);
    }

    /** PayOS recommends a short description (9 chars); keep it compact. */
    private static String buildDescription(String orderCodeLabel, long orderId) {
        String base = orderCodeLabel != null && !orderCodeLabel.isBlank()
                ? orderCodeLabel.trim()
                : ("ORD" + orderId);
        return base.length() <= 9 ? base : base.substring(0, 9);
    }

    /**
 (shipper nộp tiền về kho).
     * Unlike createCheckout(), this takes explicit returnUrl/cancelUrl.
     */
    public PayOSCheckoutResult createCheckoutForDeposit(BigDecimal amount, String description,
                                                        String returnUrl, String cancelUrl) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "PayOS is disabled. Set payos.enabled=true and configure credentials.");
        }
        if (!properties.hasCredentials()) {
            throw new IllegalStateException("PayOS credentials are missing.");
        }

        int payosOrderCode = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        long amountLong = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (amountLong <= 0) throw new IllegalArgumentException("Deposit amount must be positive");

        String desc = description != null && description.length() > 9
                ? description.substring(description.length() - 9)
                : (description == null ? "DEPOSIT" : description);

        String signature = PayOSSignature.signCreateLink(
                amountLong, cancelUrl, desc, payosOrderCode, returnUrl, properties.getChecksumKey());

        ObjectNode body = objectMapper.createObjectNode();
        body.put("orderCode", payosOrderCode);
        body.put("amount", amountLong);
        body.put("description", desc);
        body.put("cancelUrl", cancelUrl);
        body.put("returnUrl", returnUrl);
        body.put("signature", signature);

        String url = properties.getBaseUrl().replaceAll("/+$", "") + CREATE_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", properties.getClientId().trim());
        headers.set("x-api-key", properties.getApiKey().trim());

        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize PayOS deposit request", e);
        }

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("PayOS HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Invalid PayOS deposit response: " + response.getBody(), e);
        }

        String code = root.path("code").asText("");
        if (!"00".equals(code)) {
            throw new IllegalStateException("PayOS deposit link failed: " + root.path("desc").asText("error"));
        }

        JsonNode data = root.path("data");
        String checkoutUrl = data.path("checkoutUrl").asText(null);
        String paymentLinkId = data.path("paymentLinkId").asText(null);
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException("PayOS deposit response missing checkoutUrl");
        }

        return new PayOSCheckoutResult(payosOrderCode, paymentLinkId, checkoutUrl, amountLong);
    }

    public record PayOSCheckoutResult(int payosOrderCode, String paymentLinkId, String checkoutUrl, long amountVnd) {
    }
}
