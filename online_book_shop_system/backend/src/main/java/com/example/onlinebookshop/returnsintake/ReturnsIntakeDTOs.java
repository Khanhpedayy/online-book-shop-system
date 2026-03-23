package com.example.onlinebookshop.returnsintake;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReturnIntakeDTO {
    private Long id;
    private String returnCode;
    private Long orderId;
    private String orderCode;
    private String status;
    private String reason;
    private String note;
    private Double refundAmount;
    private String createdAt;
    private List<ReturnIntakeItemDTO> items;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReturnIntakeItemDTO {
    private Long id;
    private Long orderItemId;
    private Long copyId;
    private String copyCode;
    private int quantity;
    private String receivedConditionGrade;
    private String receivedConditionNote;
    private String action;
    private String titleSnapshot;
    private String skuSnapshot;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateReturnIntakeRequest {
    private Long orderId;
    private String reason;
    private String note;
    private Double refundAmount;
    private Long requestedBy;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ScanReturnCopyRequest {
    private String copyCode;
    private Long orderItemId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class RecordConditionRequest {
    private String conditionGrade;
    private String conditionNote;
}
