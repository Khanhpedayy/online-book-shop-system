package com.example.onlinebookshop.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookDetailDTO {

    private Long id;
    private String title;
    private String isbn;
    private String description;
    private String status;

    private List<BookVariantDTO> variants;
}