package com.shadentcg.shop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SalesReport {

    private final String reportType;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal totalRevenue;
    private final int totalOrderCount;
    private final int totalUnitsSold;
    private final List<ProductSalesSummary> topProducts;
    private final LocalDateTime generatedAt;

    public SalesReport(String reportType, LocalDate periodStart, LocalDate periodEnd, BigDecimal totalRevenue, int totalOrderCount, int totalUnitsSold, List<ProductSalesSummary> topProducts) {
        this.reportType = reportType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalRevenue = totalRevenue;
        this.totalOrderCount = totalOrderCount;
        this.totalUnitsSold = totalUnitsSold;
        this.topProducts = topProducts;
        this.generatedAt = LocalDateTime.now();
    }

    public String getReportType() { return reportType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public int getTotalOrderCount() { return totalOrderCount; }
    public int getTotalUnitsSold() { return totalUnitsSold; }
    public List<ProductSalesSummary> getTopProducts() { return topProducts; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public record ProductSalesSummary(String productName, int unitsSold, BigDecimal revenue) {
    }
}
