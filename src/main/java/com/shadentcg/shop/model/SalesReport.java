package com.shadentcg.shop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-holder for aggregated sales statistics (A2 data-holder class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold report period, type, total revenue, units sold, and top products</li>
 *   <li>Hold generation timestamp</li>
 *   <li>Be created by {@link SalesReportGenerator} and returned to the caller</li>
 * </ul>
 *
 * <p>Not persisted — generated on demand and held in memory for display.
 */
public class SalesReport {

    private final String reportType;       // e.g. "MONTHLY", "WEEKLY", "CUSTOM"
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal totalRevenue;
    private final int totalOrderCount;
    private final int totalUnitsSold;
    private final List<ProductSalesSummary> topProducts;
    private final LocalDateTime generatedAt;

    // ── Constructor ───────────────────────────────────────────────────

    public SalesReport(String reportType, LocalDate periodStart, LocalDate periodEnd,
                       BigDecimal totalRevenue, int totalOrderCount, int totalUnitsSold,
                       List<ProductSalesSummary> topProducts) {
        this.reportType      = reportType;
        this.periodStart     = periodStart;
        this.periodEnd       = periodEnd;
        this.totalRevenue    = totalRevenue;
        this.totalOrderCount = totalOrderCount;
        this.totalUnitsSold  = totalUnitsSold;
        this.topProducts     = topProducts;
        this.generatedAt     = LocalDateTime.now();
    }

    // ── Getters (read-only data-holder) ───────────────────────────────

    public String getReportType() { return reportType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public int getTotalOrderCount() { return totalOrderCount; }
    public int getTotalUnitsSold() { return totalUnitsSold; }
    public List<ProductSalesSummary> getTopProducts() { return topProducts; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    // ── Nested data-holder: per-product sales summary ─────────────────

    /**
     * Holds a per-product sales line for the report.
     */
    public static class ProductSalesSummary {
        private final String productName;
        private final int unitsSold;
        private final BigDecimal revenue;

        public ProductSalesSummary(String productName, int unitsSold, BigDecimal revenue) {
            this.productName = productName;
            this.unitsSold   = unitsSold;
            this.revenue     = revenue;
        }

        public String getProductName() { return productName; }
        public int getUnitsSold() { return unitsSold; }
        public BigDecimal getRevenue() { return revenue; }
    }
}
