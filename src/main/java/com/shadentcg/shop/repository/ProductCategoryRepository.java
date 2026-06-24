package com.shadentcg.shop.repository;
import com.shadentcg.shop.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findAllByOrderByNameAsc();
    boolean existsByName(String name);
}
