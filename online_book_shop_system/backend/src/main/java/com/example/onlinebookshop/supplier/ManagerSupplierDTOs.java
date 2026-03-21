package com.example.onlinebookshop.supplier;

import lombok.*;
import java.util.List;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SupplierDTO {
    private Long id;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String address;
    private String contactPerson;
    private String taxId;
    private String paymentTerms;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    private int totalLots;
    private int totalQtyReceived;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateSupplierRequest {
    private String name;
    private String code;
    private String email;
    private String phone;
    private String address;
    private String contactPerson;
    private String taxId;
    private String paymentTerms;
    private Boolean isActive;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateSupplierRequest {
    private String name;
    private String code;
    private String email;
    private String phone;
    private String address;
    private String contactPerson;
    private String taxId;
    private Boolean isActive;
    private String paymentTerms;
}
