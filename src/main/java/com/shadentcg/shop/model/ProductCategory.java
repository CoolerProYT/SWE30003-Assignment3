package com.shadentcg.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product category managed by admin (Store Manager) in A2.
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Group products into logical categories</li>
 *   <li>Provide category name and description for catalogue display</li>
 *   <li>Be managed (created/updated/removed) by the Store Manager</li>
 * </ul>
 *
 * <p>ProductCatalogue aggregates many ProductCategories; each ProductCategory
 * contains many Products (from A2 relationship table).
 */
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────

    public ProductCategory() {}

    public ProductCategory(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    @Override
    public String toString() {
        return "ProductCategory{id=" + id + ", name='" + name + "'}";
    }
}
