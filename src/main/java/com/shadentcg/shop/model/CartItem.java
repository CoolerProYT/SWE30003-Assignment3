package com.shadentcg.shop.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data-holder for a single product line within a {@link ShoppingCart} (A2 data-holder class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold a product reference (by ID) and the chosen quantity</li>
 *   <li>Snapshot product name and unit price at add-time for display</li>
 *   <li>Compute the line subtotal</li>
 * </ul>
 *
 * <p>Stored in-session (not persisted) until checkout converts to {@link OrderItem}.
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    // ── Constructors ──────────────────────────────────────────────────

    public CartItem() {}

    public CartItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        this.productId   = productId;
        this.productName = productName;
        this.unitPrice   = unitPrice;
        this.quantity    = quantity;
    }

    // ── Business logic ────────────────────────────────────────────────

    /** Returns the line subtotal (unit price × quantity). */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Adds delta to the current quantity. */
    public void increaseQuantity(int delta) {
        this.quantity += delta;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
