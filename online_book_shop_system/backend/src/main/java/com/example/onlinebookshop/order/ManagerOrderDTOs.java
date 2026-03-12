package com.example.onlinebookshop.order;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OrderListDTO {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private Double totalAmount;
    private String currency;
    private int itemCount;
    private int priority;
    private String slaDeadline;
    private String placedAt;
    private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OrderDetailDTO {
    private Long id;
    private String orderCode;
    private Long userId;
    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private String currency;
    private Double subtotalAmount;
    private Double totalAmount;
    private int priority;
    private String slaDeadline;

    // Shipping
    private String shipName;
    private String shipPhone;
    private String shipLine1;
    private String shipLine2;
    private String shipCity;
    private String shipDistrict;
    private String shipWard;

    // Customer
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Timestamps
    private String placedAt;
    private String confirmedAt;
    private String packedAt;
    private String createdAt;

    private List<OrderItemDTO> items;
    private List<OrderNoteDTO> notes;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OrderItemDTO {
    private Long id;
    private Long variantId;
    private Long copyId;
    private String copyCode;
    private int quantity;
    private Double unitPrice;
    private Double lineTotal;
    private String titleSnapshot;
    private String skuSnapshot;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OrderNoteDTO {
    private Long id;
    private Long staffId;
    private String staffName;
    private String content;
    private String createdAt;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AddNoteRequest {
    private Long staffId;
    private String content;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ConfirmOrderRequest {
    private Long staffId;
}
