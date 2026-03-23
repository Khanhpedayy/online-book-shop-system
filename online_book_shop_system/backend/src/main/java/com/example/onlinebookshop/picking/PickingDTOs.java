package com.example.onlinebookshop.picking;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PickItemDTO {
    private Long id;
    private Long orderItemId;
    private Long copyId;
    private String copyCode;
    private String location;
    private String status; // PENDING, PICKED, SKIPPED
    private String titleSnapshot;
    private String skuSnapshot;
    private String conditionGrade;
    private String pickedAt;
    private Long pickedBy;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class PickListDTO {
    private Long orderId;
    private String orderCode;
    private int totalItems;
    private int pickedItems;
    private List<PickItemDTO> items;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ManualPickRequest {
    private Long orderItemId;
    private String copyCode;
    private Long staffId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UnpickRequest {
    private String newCopyCode; // optional, if reassigning
    private Long staffId;
}
