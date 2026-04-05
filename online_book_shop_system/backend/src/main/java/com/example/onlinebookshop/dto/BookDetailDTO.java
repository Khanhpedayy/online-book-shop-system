package com.example.onlinebookshop.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDetailDTO {

    private Long id;
    private String title;
    private String isbn13;
    private String isbn10;
    private String coverImageUrl;
    private String publisherName;
    private Integer publicationYear;
    private String description;
    /** Book-level stock (same for all variants in current model). */
    private Integer stockQuantity;

    private List<BookVariantDTO> variants;

}