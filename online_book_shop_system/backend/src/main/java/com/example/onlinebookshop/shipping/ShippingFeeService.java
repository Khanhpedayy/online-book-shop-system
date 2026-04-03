package com.example.onlinebookshop.shipping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Customer shipping: free when subtotal &gt;= {@code freeAbove} from {@code settings} (SHIPPING / SHIPPING_RULES).
 * Otherwise a fixed fee ({@link #UNDER_THRESHOLD_SHIPPING_FEE_VND}). {@code standardFee} / {@code expressFee} in JSON are ignored.
 */
@Service
public class ShippingFeeService {

    public static final BigDecimal UNDER_THRESHOLD_SHIPPING_FEE_VND = new BigDecimal("10000");
    private static final BigDecimal FALLBACK_FREE_ABOVE_VND = new BigDecimal("500000");

    private final ShippingSettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    public ShippingFeeService(ShippingSettingsRepository settingsRepository, ObjectMapper objectMapper) {
        this.settingsRepository = settingsRepository;
        this.objectMapper = objectMapper;
    }

    public BigDecimal getFreeAboveThresholdVnd() {
        return settingsRepository.findShippingRulesJson()
                .map(this::parseFreeAbove)
                .orElse(FALLBACK_FREE_ABOVE_VND);
    }

    public BigDecimal computeShippingFee(BigDecimal subtotal) {
        BigDecimal s = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal freeAbove = getFreeAboveThresholdVnd();
        if (s.compareTo(freeAbove) >= 0) {
            return BigDecimal.ZERO;
        }
        return UNDER_THRESHOLD_SHIPPING_FEE_VND;
    }

    private BigDecimal parseFreeAbove(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root != null && root.hasNonNull("freeAbove")) {
                return new BigDecimal(root.get("freeAbove").asText());
            }
        } catch (Exception ignored) {
            /* use fallback */
        }
        return FALLBACK_FREE_ABOVE_VND;
    }
}
