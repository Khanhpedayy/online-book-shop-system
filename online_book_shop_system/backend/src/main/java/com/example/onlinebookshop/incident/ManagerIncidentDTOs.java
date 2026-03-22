package com.example.onlinebookshop.incident;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class IncidentDTO {
    private Long id;
    private String type;
    private Long orderId;
    private String orderCode;
    private Long copyId;
    private String copyCode;
    private String reportedByName;
    private String description;
    private String status;
    private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateIncidentRequest {
    private String type; // MISSING, DAMAGED, PICKING_ERROR, OTHER
    private Long orderId;
    private Long copyId;
    private Long reportedBy;
    private String description;
}
