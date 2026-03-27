package com.example.onlinebookshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookVariantDTO {
    private Long id;           // variant id
    private Long bookId;
    private String title;
    private String sku;
    private String isbn;
    private String publisherName;
    private Integer publicationYear;
    private String format;
    private BigDecimal salePrice;
    private BigDecimal listPrice;
    private String coverImageUrl;
    private String description;
    private String status;
}
