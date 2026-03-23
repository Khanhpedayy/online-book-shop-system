package com.example.onlinebookshop.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* â•â•â• Sales by Day (last 30 days) â•â•â• */
    public List<SalesReportDTO> getSalesByDay() {
        String sql = "SELECT CONVERT(VARCHAR(10), o.placed_at, 120) AS period, "
                + "COUNT(DISTINCT o.id) AS total_orders, "
                + "ISNULL(SUM(o.total_amount), 0) AS total_revenue, "
                + "ISNULL(SUM(oi.quantity), 0) AS total_items_sold "
                + "FROM orders o "
                + "LEFT JOIN order_items oi ON oi.order_id = o.id AND oi.deleted_at IS NULL "
                + "WHERE o.status NOT IN ('CANCELLED') AND o.deleted_at IS NULL "
                + "AND o.placed_at >= DATEADD(day, -30, GETDATE()) "
                + "GROUP BY CONVERT(VARCHAR(10), o.placed_at, 120) "
                + "ORDER BY period DESC";
        return jdbc.query(sql, (rs, i) -> {
            SalesReportDTO d = new SalesReportDTO();
            d.setPeriod(rs.getString("period"));
            d.setTotalOrders(rs.getInt("total_orders"));
            d.setTotalRevenue(rs.getDouble("total_revenue"));
            d.setTotalItemsSold(rs.getInt("total_items_sold"));
            return d;
        });
    }

    /* â•â•â• Sales by Month â•â•â• */
    public List<SalesReportDTO> getSalesByMonth() {
        String sql = "SELECT FORMAT(o.placed_at, 'yyyy-MM') AS period, "
                + "COUNT(DISTINCT o.id) AS total_orders, "
                + "ISNULL(SUM(o.total_amount), 0) AS total_revenue, "
                + "ISNULL(SUM(oi.quantity), 0) AS total_items_sold "
                + "FROM orders o "
                + "LEFT JOIN order_items oi ON oi.order_id = o.id AND oi.deleted_at IS NULL "
                + "WHERE o.status NOT IN ('CANCELLED') AND o.deleted_at IS NULL "
                + "GROUP BY FORMAT(o.placed_at, 'yyyy-MM') "
                + "ORDER BY period DESC";
        return jdbc.query(sql, (rs, i) -> {
            SalesReportDTO d = new SalesReportDTO();
            d.setPeriod(rs.getString("period"));
            d.setTotalOrders(rs.getInt("total_orders"));
            d.setTotalRevenue(rs.getDouble("total_revenue"));
            d.setTotalItemsSold(rs.getInt("total_items_sold"));
            return d;
        });
    }

    /* â•â•â• Top Selling â•â•â• */
    public List<TopSellingDTO> getTopSelling(int limit) {
        String sql = "SELECT TOP (?) b.id AS book_id, b.title, v.sku, "
                + "SUM(oi.quantity) AS total_sold, SUM(oi.quantity * oi.unit_price) AS total_revenue "
                + "FROM order_items oi "
                + "JOIN book_variants v ON oi.variant_id = v.id "
                + "JOIN books b ON v.book_id = b.id "
                + "JOIN orders o ON oi.order_id = o.id "
                + "WHERE o.status NOT IN ('CANCELLED') AND o.deleted_at IS NULL AND oi.deleted_at IS NULL "
                + "GROUP BY b.id, b.title, v.sku "
                + "ORDER BY total_sold DESC";
        return jdbc.query(sql, (rs, i) -> {
            TopSellingDTO d = new TopSellingDTO();
            d.setBookId(rs.getLong("book_id"));
            d.setTitle(rs.getString("title"));
            d.setSku(rs.getString("sku"));
            d.setTotalSold(rs.getInt("total_sold"));
            d.setTotalRevenue(rs.getDouble("total_revenue"));
            return d;
        }, limit);
    }

    /* â•â•â• Slow Movers â•â•â• */
    public List<SlowMoverDTO> getSlowMovers() {
        String sql = "SELECT v.id AS variant_id, v.sku, b.title, "
                + "ISNULL(SUM(l.qty_available), 0) AS qty_available, "
                + "ISNULL(sold.qty_sold_30, 0) AS qty_sold_last_30, "
                + "ISNULL(sold.days_since, 999) AS days_since_last_sale "
                + "FROM book_variants v "
                + "JOIN books b ON v.book_id = b.id "
                + "LEFT JOIN lots l ON l.variant_id = v.id AND l.deleted_at IS NULL "
                + "LEFT JOIN ("
                + "  SELECT oi.variant_id, SUM(oi.quantity) AS qty_sold_30, "
                + "  DATEDIFF(day, MAX(o.placed_at), GETDATE()) AS days_since "
                + "  FROM order_items oi "
                + "  JOIN orders o ON oi.order_id = o.id "
                + "  WHERE o.status NOT IN ('CANCELLED') AND o.placed_at >= DATEADD(day, -30, GETDATE()) "
                + "  GROUP BY oi.variant_id"
                + ") sold ON sold.variant_id = v.id "
                + "WHERE v.deleted_at IS NULL AND b.deleted_at IS NULL "
                + "GROUP BY v.id, v.sku, b.title, sold.qty_sold_30, sold.days_since "
                + "HAVING ISNULL(SUM(l.qty_available), 0) > 0 AND ISNULL(sold.qty_sold_30, 0) <= 2 "
                + "ORDER BY ISNULL(sold.qty_sold_30, 0) ASC, ISNULL(SUM(l.qty_available), 0) DESC";
        return jdbc.query(sql, (rs, i) -> {
            SlowMoverDTO d = new SlowMoverDTO();
            d.setVariantId(rs.getLong("variant_id"));
            d.setSku(rs.getString("sku"));
            d.setTitle(rs.getString("title"));
            d.setQtyAvailable(rs.getInt("qty_available"));
            d.setQtySoldLast30Days(rs.getInt("qty_sold_last_30"));
            d.setDaysSinceLastSale(rs.getLong("days_since_last_sale"));
            return d;
        });
    }

    /* â•â•â• Lot Aging â•â•â• */
    public List<LotAgingDTO> getLotAging() {
        String sql = "SELECT l.id AS lot_id, l.lot_code, v.sku AS variant_sku, b.title, s.name AS supplier_name, "
                + "l.qty_available, DATEDIFF(day, l.received_at, GETDATE()) AS age_days, "
                + "(l.qty_available * l.unit_cost) AS total_cost_value "
                + "FROM lots l "
                + "JOIN book_variants v ON l.variant_id = v.id "
                + "JOIN books b ON v.book_id = b.id "
                + "JOIN suppliers s ON l.supplier_id = s.id "
                + "WHERE l.deleted_at IS NULL AND l.qty_available > 0 "
                + "ORDER BY age_days DESC";
        return jdbc.query(sql, (rs, i) -> {
            LotAgingDTO d = new LotAgingDTO();
            d.setLotId(rs.getLong("lot_id"));
            d.setLotCode(rs.getString("lot_code"));
            d.setVariantSku(rs.getString("variant_sku"));
            d.setTitle(rs.getString("title"));
            d.setSupplierName(rs.getString("supplier_name"));
            d.setQtyAvailable(rs.getInt("qty_available"));
            d.setAgeDays(rs.getLong("age_days"));
            d.setTotalCostValue(rs.getDouble("total_cost_value"));
            // Bucket
            long days = d.getAgeDays();
            if (days <= 30)
                d.setAgeBucket("0-30");
            else if (days <= 60)
                d.setAgeBucket("31-60");
            else if (days <= 90)
                d.setAgeBucket("61-90");
            else
                d.setAgeBucket("90+");
            return d;
        });
    }

    /* â•â•â• Inventory Value â•â•â• */
    public List<InventoryValueDTO> getInventoryValue() {
        String sql = "SELECT v.id AS variant_id, v.sku, b.title, "
                + "ISNULL(SUM(l.qty_available), 0) AS total_qty_available, "
                + "ISNULL(AVG(l.unit_cost), 0) AS avg_unit_cost, "
                + "ISNULL(SUM(l.qty_available * l.unit_cost), 0) AS total_cost_value, "
                + "ISNULL(SUM(l.qty_available), 0) * v.sale_price AS total_retail_value "
                + "FROM book_variants v "
                + "JOIN books b ON v.book_id = b.id "
                + "LEFT JOIN lots l ON l.variant_id = v.id AND l.deleted_at IS NULL "
                + "WHERE v.deleted_at IS NULL AND b.deleted_at IS NULL "
                + "GROUP BY v.id, v.sku, b.title, v.sale_price "
                + "HAVING ISNULL(SUM(l.qty_available), 0) > 0 "
                + "ORDER BY total_cost_value DESC";
        return jdbc.query(sql, (rs, i) -> {
            InventoryValueDTO d = new InventoryValueDTO();
            d.setVariantId(rs.getLong("variant_id"));
            d.setSku(rs.getString("sku"));
            d.setTitle(rs.getString("title"));
            d.setTotalQtyAvailable(rs.getInt("total_qty_available"));
            d.setAvgUnitCost(rs.getDouble("avg_unit_cost"));
            d.setTotalCostValue(rs.getDouble("total_cost_value"));
            d.setTotalRetailValue(rs.getDouble("total_retail_value"));
            return d;
        });
    }

    /* â•â•â• Shrinkage â•â•â• */
    public List<ShrinkageDTO> getShrinkage() {
        String sql = "SELECT it.reason, SUM(it.quantity) AS total_qty, "
                + "COUNT(*) AS incident_count "
                + "FROM inventory_transactions it "
                + "WHERE it.movement_type = 'ADJUST' AND it.reason IN ('DAMAGED','LOST','COUNT_DIFF') "
                + "GROUP BY it.reason "
                + "ORDER BY total_qty DESC";
        return jdbc.query(sql, (rs, i) -> {
            ShrinkageDTO d = new ShrinkageDTO();
            d.setReason(rs.getString("reason"));
            d.setTotalQty(rs.getInt("total_qty"));
            d.setIncidentCount(rs.getInt("incident_count"));
            d.setEstimatedLoss(0.0); // Could calculate estimated loss from lot costs
            return d;
        });
    }

    /* â•â•â• Dashboard Summary â•â•â• */
    public DashboardSummaryDTO getDashboardSummary() {
        DashboardSummaryDTO s = new DashboardSummaryDTO();

        jdbc.query("SELECT COUNT(*) AS cnt FROM books WHERE deleted_at IS NULL", (rs, i) -> {
            s.setTotalBooks(rs.getInt("cnt"));
            return null;
        });
        jdbc.query("SELECT COUNT(*) AS cnt FROM book_variants WHERE deleted_at IS NULL", (rs, i) -> {
            s.setTotalVariants(rs.getInt("cnt"));
            return null;
        });
        jdbc.query("SELECT COUNT(*) AS cnt FROM copies WHERE status = 'AVAILABLE' AND deleted_at IS NULL", (rs, i) -> {
            s.setTotalCopiesAvailable(rs.getInt("cnt"));
            return null;
        });
        jdbc.query("SELECT ISNULL(SUM(l.qty_available * l.unit_cost), 0) AS val FROM lots l WHERE deleted_at IS NULL",
                (rs, i) -> {
                    s.setTotalInventoryValue(rs.getDouble("val"));
                    return null;
                });
        jdbc.query(
                "SELECT COUNT(*) AS cnt, ISNULL(SUM(total_amount), 0) AS rev FROM orders WHERE status != 'CANCELLED' AND deleted_at IS NULL",
                (rs, i) -> {
                    s.setTotalOrders(rs.getInt("cnt"));
                    s.setTotalRevenue(rs.getDouble("rev"));
                    return null;
                });
        return s;
    }
}

