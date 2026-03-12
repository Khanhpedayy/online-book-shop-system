package com.example.onlinebookshop.stocktaking;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class StocktakingSessionDTO {
    private String sessionCode;
    private String status; // OPEN, COMPLETED
    private String scope;
    private String note;
    private String createdAt;
    private String completedAt;
    private List<StocktakingEntryDTO> entries;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class StocktakingEntryDTO {
    private Long variantId;
    private String sku;
    private String title;
    private Long lotId;
    private String lotCode;
    private int expectedQty;
    private Integer countedQty;
    private Integer diff;
    private String note;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateStocktakingRequest {
    private String scope; // ALL, VARIANT:{id}, LOT:{id}
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class RecordCountRequest {
    private Long variantId;
    private Long lotId;
    private int countedQty;
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ApplyAdjustmentsRequest {
    private String note;
}
