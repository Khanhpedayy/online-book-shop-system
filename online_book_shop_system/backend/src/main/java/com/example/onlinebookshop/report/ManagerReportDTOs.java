package com.example.onlinebookshop.report;

import lombok.*;
import java.util.List;

/* ═══ Sales Report ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SalesReportDTO {
    private String period; // day/month label
    private int totalOrders;
    private Double totalRevenue;
    private int totalItemsSold;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TopSellingDTO {
    private Long bookId;
    private String title;
    private String sku;
    private int totalSold;
    private Double totalRevenue;
}

/* ═══ Slow Movers ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SlowMoverDTO {
    private Long variantId;
    private String sku;
    private String title;
    private int qtyAvailable;
    private int qtySoldLast30Days;
    private Long daysSinceLastSale;
}

/* ═══ Lot Aging ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LotAgingDTO {
    private Long lotId;
    private String lotCode;
    private String variantSku;
    private String title;
    private String supplierName;
    private int qtyAvailable;
    private Long ageDays;
    private String ageBucket; // 0-30, 31-60, 61-90, 90+
    private Double totalCostValue;
}

/* ═══ Inventory Value ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class InventoryValueDTO {
    private Long variantId;
    private String sku;
    private String title;
    private int totalQtyAvailable;
    private Double avgUnitCost;
    private Double totalCostValue;
    private Double totalRetailValue;
}

/* ═══ Shrinkage ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ShrinkageDTO {
    private String reason; // DAMAGED, LOST, COUNT_DIFF
    private int totalQty;
    private Double estimatedLoss;
    private int incidentCount;
}

/* ═══ Summary ═══ */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class DashboardSummaryDTO {
    private int totalBooks;
    private int totalVariants;
    private int totalCopiesAvailable;
    private Double totalInventoryValue;
    private int totalOrders;
    private Double totalRevenue;
    private int lowStockCount;
    private int overstockCount;
}
