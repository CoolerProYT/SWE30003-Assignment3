package com.shadentcg.shop.repository;
import com.shadentcg.shop.model.Shipment;
import com.shadentcg.shop.model.Shipment.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    List<Shipment> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);
    List<Shipment> findAllByOrderByCreatedAtDesc();
}
