package com.example.onlinebookshop.payos;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payos")
public class PayOSProperties {

    /**
     * When false, PayOS checkout is rejected with a clear configuration message.
     */
    private boolean enabled = false;

    private String clientId = "";
    private String apiKey = "";
    private String checksumKey = "";

    /**
     * API base, e.g. https://api-merchant.payos.vn
     */
    private String baseUrl = "https://api-merchant.payos.vn";

    /**
     * Must contain {@code {orderId}}; PayOS will append query params (code, status, cancel, orderCode, …).
     */
    private String returnUrlTemplate = "http://localhost:8080/payment-result.html?orderId={orderId}";

    private String cancelUrlTemplate = "http://localhost:8080/payment-result.html?orderId={orderId}&payosCancel=1";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getChecksumKey() {
        return checksumKey;
    }

    public void setChecksumKey(String checksumKey) {
        this.checksumKey = checksumKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getReturnUrlTemplate() {
        return returnUrlTemplate;
    }

    public void setReturnUrlTemplate(String returnUrlTemplate) {
        this.returnUrlTemplate = returnUrlTemplate;
    }

    public String getCancelUrlTemplate() {
        return cancelUrlTemplate;
    }

    public void setCancelUrlTemplate(String cancelUrlTemplate) {
        this.cancelUrlTemplate = cancelUrlTemplate;
    }

    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && checksumKey != null && !checksumKey.isBlank();
    }
}
