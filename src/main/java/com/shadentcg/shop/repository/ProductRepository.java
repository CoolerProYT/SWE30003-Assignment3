package com.shadentcg.shop.repository;
import com.shadentcg.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.available = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:k,'%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%',:k,'%')))")
    List<Product> searchAvailable(@Param("k") String keyword);
}
