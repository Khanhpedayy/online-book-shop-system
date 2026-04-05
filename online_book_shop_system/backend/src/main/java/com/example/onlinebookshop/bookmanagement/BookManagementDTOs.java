package com.example.onlinebookshop.bookmanagement;

import lombok.*;
import java.util.List;

/* â•â•â• Response DTOs â•â•â• */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BookListItemDTO {
    private Long id;
    private String isbn13;
    private String title;
    private String subtitle;
    private String slug;
    private String publisherName;
    private Integer publicationYear;
    private String language;
    private String shortDescription;
    private String status; // ACTIVE, HIDDEN, DRAFT
    private String categoryName;
    private Long categoryId;
    private String coverImageUrl;
    private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BookDetailDTO {
    private Long id;
    private String isbn13;
    private String isbn10;
    private String title;
    private String subtitle;
    private String slug;
    private String publisherName;
    private Integer publicationYear;
    private String language;
    private String shortDescription;
    private String descriptionHtml;
    private String status;
    private Long categoryId;
    private String categoryName;
    private String createdAt;
    private String updatedAt;

    private List<BookAuthorDTO> authors;
    private List<BookVariantDTO> variants;
    private List<BookImageDTO> images;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BookAuthorDTO {
    private Long authorId;
    private String name;
    private String role; // AUTHOR, TRANSLATOR, etc.
    private Integer sortOrder;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BookVariantDTO {
    private Long id;
    private String sku;
    private String format; // HARDCOVER, PAPERBACK, BOXSET
    private String edition;
    private String language;
    private Double listPrice;
    private Double salePrice;
    private Integer pageCount;
    private Integer weightGrams;
    private Boolean isActive;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BookImageDTO {
    private Long id;
    private String url;
    private String altText;
    private Boolean isCover;
    private Integer sortOrder;
    private Long variantId;
}

/* â•â•â• Request DTOs â•â•â• */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateBookRequest {
    private String isbn13;
    private String isbn10;
    private String title;
    private String subtitle;
    private String publisherName;
    private Integer publicationYear;
    private String language;
    private String shortDescription;
    private String descriptionHtml;
    private String status; // ACTIVE, HIDDEN, DRAFT
    private Long categoryId;

    private List<AuthorInput> authors;
    private List<VariantInput> variants;
    private List<ImageInput> images;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UpdateBookRequest {
    private String isbn13;
    private String isbn10;
    private String title;
    private String subtitle;
    private String publisherName;
    private Integer publicationYear;
    private String language;
    private String shortDescription;
    private String descriptionHtml;
    private String status; // ACTIVE, HIDDEN, DRAFT
    private Long categoryId;

    private List<ImageInput> images; // full replace on update
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AuthorInput {
    private Long authorId;
    private String role; // AUTHOR, TRANSLATOR, ILLUSTRATOR, EDITOR
    private Integer sortOrder;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class VariantInput {
    private String sku;
    private String format;
    private String edition;
    private String language;
    private Double listPrice;
    private Double salePrice;
    private Integer pageCount;
    private Integer weightGrams;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ImageInput {
    private String url;
    private String altText;
    private Boolean isCover;
    private Integer sortOrder;
    private Long variantId; // null = book-level image
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ChangeStatusRequest {
    private String status; // ACTIVE | HIDDEN | DRAFT
}

