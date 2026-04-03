package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.shipping.ShippingFeeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingQuoteController {

    private final ShippingFeeService shippingFeeService;

    public ShippingQuoteController(ShippingFeeService shippingFeeService) {
        this.shippingFeeService = shippingFeeService;
    }

    /**
     * Quote shipping for a cart subtotal (VND). Public so the storefront can match checkout/order totals.
     */
    @GetMapping("/quote")
    public Map<String, Object> quote(@RequestParam(required = false) String subtotal) {
        BigDecimal s = parseSubtotal(subtotal);
        BigDecimal fee = shippingFeeService.computeShippingFee(s);
        BigDecimal freeAbove = shippingFeeService.getFreeAboveThresholdVnd();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subtotal", s);
        body.put("freeAbove", freeAbove);
        body.put("defaultShippingFee", ShippingFeeService.UNDER_THRESHOLD_SHIPPING_FEE_VND);
        body.put("shippingFee", fee);
        body.put("freeShipping", fee.compareTo(BigDecimal.ZERO) == 0);
        return body;
    }

    private static BigDecimal parseSubtotal(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
