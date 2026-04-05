package com.example.onlinebookshop.copy;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CopyDTO {
    private Long id;
    private String copyCode;
    private Long lotId;
    private String lotCode;
    private Long variantId;
    private String variantSku;
    private String bookTitle;
    private String location;
    private String conditionGrade;
    private String conditionNote;
    private Boolean hasSignature;
    private Boolean isFirstEdition;
    private String attributesJson;
    private String imagesJson;
    private Double sellPriceOverride;
    private String status;
    private String reservedAt;
    private String reserveExpiresAt;
    private String createdAt;
    private String updatedAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CopyLifecycleDTO extends CopyDTO {
    private String supplierName;
    private String receivedAt;
    private Double unitCost;
    private List<CopyTransactionDTO> transactions;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CopyTransactionDTO {
    private Long id;
    private String movementType;
    private int quantity;
    private String fromLocation;
    private String toLocation;
    private String referenceType;
    private Long referenceId;
    private String reason;
    private String note;
    private String createdAt;
}

/* ═══ Request ═══ */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MoveLocationRequest {
    private String newLocation;
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MarkStatusRequest {
    private String status; // DAMAGED, LOST, AVAILABLE (found)
    private String reason;
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AttachPhotosRequest {
    private String imagesJson; // JSON array of image URLs
}
