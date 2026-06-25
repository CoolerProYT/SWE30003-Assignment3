package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Product;
import com.shadentcg.shop.model.ProductCategory;
import com.shadentcg.shop.repository.ProductCategoryRepository;
import com.shadentcg.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<Product> getAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllAvailable();
        }
        return productRepository.searchAvailable(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllForAdmin() {
        return productRepository.findAll();
    }

    public Product createProduct(String name, String description, BigDecimal price, int stockQuantity, Long categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return productRepository.save(
            new Product(name, description, price, stockQuantity, category));
    }

    public Product updateProduct(Long id, String name, String description, BigDecimal price, int stockQuantity, Long categoryId) {
        Product product = getProductById(id);
        ProductCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        return productRepository.save(product);
    }

    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setAvailable(false);
        productRepository.save(product);
    }

    public void reactivateProduct(Long id) {
        Product product = getProductById(id);
        if (product.getStockQuantity() > 0) {
            product.setAvailable(true);
            productRepository.save(product);
        }
    }

    public ProductCategory createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category '" + name + "' already exists.");
        }
        return categoryRepository.save(new ProductCategory(name, description));
    }
}
