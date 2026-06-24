package com.shadentcg.shop.repository;
import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId);
    List<Order> findByStatusOrderByPlacedAtDesc(OrderStatus status);
}
