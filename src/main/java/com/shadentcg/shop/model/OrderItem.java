package com.shadentcg.shop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a single product line within an {@link Order} (A2 class).
 *
 * <p>Snapshots product name and unit price at order time so historical
 * orders remain accurate if the product is later updated.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Snapshot of product name at order time. */
    @Column(nullable = false)
    private String productName;

    /** Snapshot of unit price at order time. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    // ── Constructors ──────────────────────────────────────────────────

    public OrderItem() {}

    public OrderItem(Product product, int quantity) {
        this.product     = product;
        this.productName = product.getName();
        this.unitPrice   = product.getPrice();
        this.quantity    = quantity;
    }

    // ── Business logic ────────────────────────────────────────────────

    /** Returns the line subtotal (unit price × quantity). */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
