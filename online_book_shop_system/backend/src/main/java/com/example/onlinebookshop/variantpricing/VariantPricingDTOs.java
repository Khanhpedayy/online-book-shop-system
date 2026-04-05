package com.example.onlinebookshop.variantpricing;

import lombok.*;
import java.util.List;

/* ═══════ Response DTOs ═══════ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class VariantDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String sku;
    private String format; // HARDCOVER, PAPERBACK, BOXSET
    private String edition;
    private String language;
    private Double listPrice;
    private Double salePrice;
    private Integer pageCount;
    private Integer weightGrams;
    private Integer widthMm;
    private Integer heightMm;
    private Integer thicknessMm;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    // computed fields
    private Integer totalCopies;
    private Integer availableCopies;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CopyPricingDTO {
    private Long id;
    private String copyCode;
    private Long variantId;
    private String variantSku;
    private Long lotId;
    private String lotCode;
    private String conditionGrade;
    private String conditionNote;
    private Boolean hasSignature;
    private Boolean isFirstEdition;
    private Double sellPriceOverride;
    private String status;
    private String location;
    private String createdAt;
}

/* ═══════ Request DTOs ═══════ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateVariantRequest {
    private Long bookId;
    private String sku;
    private String format; // HARDCOVER | PAPERBACK | BOXSET
    private String edition;
    private String language;
    private Double listPrice;
    private Double salePrice;
    private Integer pageCount;
    private Integer weightGrams;
    private Integer widthMm;
    private Integer heightMm;
    private Integer thicknessMm;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateVariantRequest {
    private String sku;
    private String format;
    private String edition;
    private String language;
    private Double listPrice;
    private Double salePrice;
    private Integer pageCount;
    private Integer weightGrams;
    private Integer widthMm;
    private Integer heightMm;
    private Integer thicknessMm;
    private Boolean isActive;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SetBasePriceRequest {
    private Double listPrice;
    private Double salePrice;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OverrideCopyPriceRequest {
    private Double sellPriceOverride; // null to clear override
}
