package com.example.onlinebookshop;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String title;
    @Column(name = "isbn13")
    private String isbn;
    @Column(name = "list_price")
    private Double price;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String description;

    private String status; // ACTIVE / HIDDEN / DRAFT
}

