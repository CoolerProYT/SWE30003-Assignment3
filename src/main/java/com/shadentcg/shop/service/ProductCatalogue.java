package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Product;
import com.shadentcg.shop.model.ProductCategory;
import com.shadentcg.shop.repository.ProductCategoryRepository;
import com.shadentcg.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides search and browse access to the product catalogue (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Provide the single entry point for all catalogue browsing and search
 *       (Information Expert heuristic: catalogue-level concerns stay here)</li>
 *   <li>Return available products for customer browsing</li>
 *   <li>Support keyword search across name and description</li>
 *   <li>Filter by {@link ProductCategory}</li>
 *   <li>Manage the full product catalogue for admin (Store Manager): add, edit, deactivate</li>
 * </ul>
 *
 * <p>A2 relationship: ProductCatalogue manages many Products.
 */
@Service
@Transactional
public class ProductCatalogue {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductCatalogue(ProductRepository productRepository,
                            ProductCategoryRepository categoryRepository) {
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ── Customer browsing ─────────────────────────────────────────────

    /** Returns all available (in-stock) products. */
    @Transactional(readOnly = true)
    public List<Product> getAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    /**
     * Returns available products filtered by category.
     *
     * @param categoryId the category ID to filter by
     * @return matching available products
     */
    @Transactional(readOnly = true)
    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    /**
     * Full-text search across product name and description (case-insensitive).
     *
     * @param keyword search term; returns all available products if blank
     * @return matching available products
     */
    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllAvailable();
        }
        return productRepository.searchAvailable(keyword.trim());
    }

    /** Returns all product categories for the filter UI. */
    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id the product ID
     * @return the Product
     * @throws IllegalArgumentException if not found
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    // ── Admin (Store Manager) product management ──────────────────────

    /** Returns all products including inactive ones (admin view). */
    @Transactional(readOnly = true)
    public List<Product> getAllForAdmin() {
        return productRepository.findAll();
    }

    /**
     * Creates a new product in the catalogue.
     *
     * @param name          product name
     * @param description   product description
     * @param price         unit price
     * @param stockQuantity initial stock
     * @param categoryId    the category this product belongs to
     * @return the persisted Product
     */
    public Product createProduct(String name, String description, BigDecimal price,
                                 int stockQuantity, Long categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return productRepository.save(
            new Product(name, description, price, stockQuantity, category));
    }

    /**
     * Updates an existing product's details.
     *
     * @param id            product ID
     * @param name          updated name
     * @param description   updated description
     * @param price         updated price
     * @param stockQuantity updated stock
     * @param categoryId    updated category
     * @return the updated Product
     */
    public Product updateProduct(Long id, String name, String description,
                                 BigDecimal price, int stockQuantity, Long categoryId) {
        Product product = getProductById(id);
        ProductCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        return productRepository.save(product);
    }

    /** Soft-deletes a product by marking it unavailable. */
    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setAvailable(false);
        productRepository.save(product);
    }

    /** Re-activates a previously deactivated product. */
    public void reactivateProduct(Long id) {
        Product product = getProductById(id);
        if (product.getStockQuantity() > 0) {
            product.setAvailable(true);
            productRepository.save(product);
        }
    }

    // ── Category management ───────────────────────────────────────────

    /** Creates a new product category. */
    public ProductCategory createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists.");
        }
        return categoryRepository.save(new ProductCategory(name, description));
    }
}
