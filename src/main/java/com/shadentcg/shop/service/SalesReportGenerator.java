package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.SalesReport;
import com.shadentcg.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates order data and produces sales statistics reports (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Aggregate order and payment data for a given period</li>
 *   <li>Produce a {@link SalesReport} data-holder instance</li>
 *   <li>Support monthly, weekly, and custom date-range reports</li>
 * </ul>
 *
 * <p>A2 bootstrap step 6: SalesReportGenerator created at startup.
 * A2 bootstrap step 12: creates SalesReport on demand and returns it.
 */
@Service
@Transactional(readOnly = true)
public class SalesReportGenerator {

    private final OrderRepository orderRepository;

    public SalesReportGenerator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Generates a sales report for the current calendar month.
     *
     * @return SalesReport data-holder
     */
    public SalesReport generateMonthlyReport() {
        LocalDate now   = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end   = now.withDayOfMonth(now.lengthOfMonth());
        return generateReport("MONTHLY", start, end);
    }

    /**
     * Generates a sales report for a custom date range.
     *
     * @param start period start (inclusive)
     * @param end   period end (inclusive)
     * @return SalesReport data-holder
     */
    public SalesReport generateCustomReport(LocalDate start, LocalDate end) {
        return generateReport("CUSTOM", start, end);
    }

    // ── Private aggregation logic ─────────────────────────────────────

    private SalesReport generateReport(String type, LocalDate start, LocalDate end) {
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> {
                LocalDate placed = o.getPlacedAt().toLocalDate();
                return !placed.isBefore(start) && !placed.isAfter(end)
                    && o.getStatus() != Order.OrderStatus.CANCELLED;
            })
            .collect(Collectors.toList());

        BigDecimal totalRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUnits = orders.stream()
            .flatMap(o -> o.getItems().stream())
            .mapToInt(i -> i.getQuantity())
            .sum();

        // Per-product aggregation
        Map<String, int[]> productSales = new LinkedHashMap<>();
        Map<String, BigDecimal> productRevenue = new LinkedHashMap<>();
        orders.stream()
            .flatMap(o -> o.getItems().stream())
            .forEach(item -> {
                String name = item.getProductName();
                productSales.computeIfAbsent(name, k -> new int[]{0})[0] += item.getQuantity();
                productRevenue.merge(name, item.getSubtotal(), BigDecimal::add);
            });

        List<SalesReport.ProductSalesSummary> topProducts = productSales.entrySet().stream()
            .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
            .limit(5)
            .map(e -> new SalesReport.ProductSalesSummary(
                e.getKey(), e.getValue()[0],
                productRevenue.getOrDefault(e.getKey(), BigDecimal.ZERO)))
            .collect(Collectors.toList());

        return new SalesReport(type, start, end, totalRevenue,
                               orders.size(), totalUnits, topProducts);
    }
}
