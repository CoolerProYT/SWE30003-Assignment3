package com.shadentcg.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Represents a purchasable item in the Shaden TCG Shop.
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold product details: name, description, price, stock quantity</li>
 *   <li>Manage stock: reserve, deduct, and restock quantities</li>
 *   <li>Belong to a {@link ProductCategory}</li>
 *   <li>Indicate availability for catalogue display</li>
 * </ul>
 *
 * <p>A2 relationship: Product belongs-to ProductCategory (many-to-one),
 * ProductCatalogue manages many Products.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    @Column(nullable = false)
    private String name;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity;

    /** A2: Product belongs-to ProductCategory (many Products per category). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(nullable = false)
    private boolean available = true;

    // ── Constructors ──────────────────────────────────────────────────

    public Product() {}

    public Product(String name, String description, BigDecimal price,
                   Integer stockQuantity, ProductCategory category) {
        this.name          = name;
        this.description   = description;
        this.price         = price;
        this.stockQuantity = stockQuantity;
        this.category      = category;
    }

    // ── Business logic ────────────────────────────────────────────────

    /**
     * Returns true if the product can fulfil the requested quantity.
     *
     * @param requestedQty units the customer wants
     * @return true if sufficient stock exists
     */
    public boolean hasStock(int requestedQty) {
        return available && stockQuantity >= requestedQty;
    }

    /**
     * Deducts stock after a successful order. Marks unavailable if stock reaches zero.
     *
     * @param qty units to deduct
     * @throws IllegalStateException if insufficient stock
     */
    public void deductStock(int qty) {
        if (!hasStock(qty)) {
            throw new IllegalStateException(
                "Insufficient stock for: " + name + " (requested " + qty
                    + ", available " + stockQuantity + ")");
        }
        this.stockQuantity -= qty;
        if (this.stockQuantity == 0) {
            this.available = false;
        }
    }

    /**
     * Restores stock (e.g. after order cancellation).
     *
     * @param qty units to restore
     */
    public void restoreStock(int qty) {
        this.stockQuantity += qty;
        if (this.stockQuantity > 0) {
            this.available = true;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        if (stockQuantity != null && stockQuantity > 0) this.available = true;
    }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}
