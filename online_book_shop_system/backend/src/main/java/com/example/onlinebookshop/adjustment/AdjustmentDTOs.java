package com.example.onlinebookshop.adjustment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AdjustmentDTO {
    private Long id;
    private String type;
    private String direction;
    private Long variantId;
    private String variantSku;
    private String bookTitle;
    private Long lotId;
    private String lotCode;
    private Long copyId;
    private String copyCode;
    private int quantity;
    private String fromLocation;
    private String toLocation;
    private String referenceType;
    private Long referenceId;
    private String reason;
    private String note;
    private String createdAt;
    private String createdByName;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateAdjustmentRequest {
    private Long variantId;
    private Long lotId;
    private Long copyId;
    private String type;
    private String direction;
    private int quantity;
    private String reason;
    private String note;
    private Long createdBy;
}
