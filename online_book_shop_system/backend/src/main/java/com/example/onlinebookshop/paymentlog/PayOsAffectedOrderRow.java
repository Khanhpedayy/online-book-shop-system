package com.example.onlinebookshop.paymentlog;

/**
 * Snapshot of an order before PayOS payment status sync (webhook / return URL).
 */
public record PayOsAffectedOrderRow(long orderId, String paymentMethod, String previousPaymentStatus) {
}
