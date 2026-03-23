package com.example.onlinebookshop.dashboard;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class DashboardDTO {
    /* Books */
    private int totalBooks;
    private int activeBooks;
    private int draftBooks;

    /* Categories */
    private int totalCategories;
    private int activeCategories;

    /* Stock */
    private int totalStockQuantity;
    private int outOfStockBooks;
    private int lowStockBooks;      // stock_quantity between 1 and 5

    /* Recent books */
    private List<RecentBookDTO> recentBooks;

    /* Stock alerts */
    private List<StockAlertDTO> stockAlerts;

    /* Category distribution */
    private List<CategoryStatsDTO> categoryStats;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class RecentBookDTO {
    private Long id;
    private String title;
    private String categoryName;
    private String status;
    private int stockQuantity;
    private String createdAt;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class StockAlertDTO {
    private Long bookId;
    private String title;
    private int stockQuantity;
    private String alertType; // OUT_OF_STOCK, LOW_STOCK
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class CategoryStatsDTO {
    private String categoryName;
    private int bookCount;
    private int totalStock;
}
