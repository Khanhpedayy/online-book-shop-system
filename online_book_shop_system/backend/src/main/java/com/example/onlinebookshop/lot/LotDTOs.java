package com.example.onlinebookshop.lot;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LotDTO {
    private Long id;
    private String lotCode;
    private Long supplierId;
    private String supplierName;
    private Long variantId;
    private String variantSku;
    private String bookTitle;
    private String receiptCode;
    private String invoiceNo;
    private String warehouse;
    private String receivedAt;
    private Double unitCost;
    private int qtyReceived;
    private int qtyAvailable;
    private int qtyReserved;
    private int qtySold;
    private int qtyDamaged;
    private int qtyReturned;
    private String conditionDefault;
    private String status;
    private String note;
    private String createdAt;
    private Long ageDays;
    private Double totalCostValue;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LotDetailDTO extends LotDTO {
    private List<LotCopyDTO> copies;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LotCopyDTO {
    private Long id;
    private String copyCode;
    private String location;
    private String conditionGrade;
    private String conditionNote;
    private String status;
    private Double sellPriceOverride;
    private String createdAt;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateLotRequest {
    private String lotCode;
    private Long supplierId;
    private Long variantId;
    private String receiptCode;
    private String invoiceNo;
    private String warehouse;
    private String receivedAt;
    private Double unitCost;
    private int qtyReceived;
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class GenerateCopiesRequest {
    private String prefix;
    private String defaultLocation;
    private String conditionGrade;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LockLotRequest {
    private String reason;
}
