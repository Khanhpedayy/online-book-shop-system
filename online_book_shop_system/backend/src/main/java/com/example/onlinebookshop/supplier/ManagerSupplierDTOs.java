package com.example.onlinebookshop.supplier;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
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
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    private int totalLots;
    private int totalQtyReceived;

    // Additional fields mapped from db
    private String taxId;
    private String paymentTerms;
}

/* ═══ Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateSupplierRequest {
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    private String code;

    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    private String phone;
    private String address;
    private String contactPerson;
    private Boolean isActive;

    // Extra fields to prevent Jackson UnrecognizedPropertyException
    private String taxId;
    private String paymentTerms;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateSupplierRequest {
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    private String code;

    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    private String phone;
    private String address;
    private String contactPerson;
    private Boolean isActive;

    // Extra fields
    private String taxId;
    private String paymentTerms;
}
