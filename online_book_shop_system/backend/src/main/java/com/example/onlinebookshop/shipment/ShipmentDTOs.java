package com.example.onlinebookshop.shipment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ShipmentDTO {
    private Long id;
    private Long orderId;
    private String carrier;
    private String trackingCode;
    private int boxCount;
    private String status;
    private String shippedAt;
    private String deliveredAt;
    private String note;
    private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateShipmentRequest {
    private String carrier;
    private String trackingCode;
    private int boxCount;
    private String note;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateDeliveryRequest {
    private String outcome; // DELIVERED or FAILED
    private String note;
}
