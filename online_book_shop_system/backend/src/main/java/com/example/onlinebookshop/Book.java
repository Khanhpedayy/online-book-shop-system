package com.example.onlinebookshop;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;
    private String isbn;
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer stockQuantity;
    private String status; // active / inactive
}
