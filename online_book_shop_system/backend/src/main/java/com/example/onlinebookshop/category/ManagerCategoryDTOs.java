package com.example.onlinebookshop.category;

import lombok.*;

/* ═══ Response ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
    private String createdAt;
    private String updatedAt;
    private int bookCount;
}

/* ═══ Create Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateCategoryRequest {
    private String name;
    private String description;
    private Integer sortOrder;
}

/* ═══ Update Request ═══ */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateCategoryRequest {
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;
}
