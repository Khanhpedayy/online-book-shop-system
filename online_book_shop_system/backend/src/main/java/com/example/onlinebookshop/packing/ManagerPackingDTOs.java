package com.example.onlinebookshop.packing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PackingStatusDTO {
    private Long orderId;
    private String orderCode;
    private boolean allPicked;
    private boolean packingConfirmed;
    private int boxCount;
    private String packedAt;
    private String status; // PENDING_PICK, READY_TO_PACK, PACKED
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PackingSlipDTO {
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String shipAddress;
    private java.util.List<PackingSlipItemDTO> items;
    private Double totalAmount;
    private String currency;
    private String packedAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PackingSlipItemDTO {
    private String title;
    private String sku;
    private int quantity;
    private Double unitPrice;
    private Double lineTotal;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AssignPackagesRequest {
    private int boxCount;
    private String note;
}
