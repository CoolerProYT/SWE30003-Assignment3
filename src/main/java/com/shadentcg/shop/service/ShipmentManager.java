package com.shadentcg.shop.service;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.observer.OrderEventListener;
import com.shadentcg.shop.repository.OrderRepository;
import com.shadentcg.shop.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ShipmentManager {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderEventListener notificationService;

    public ShipmentManager(ShipmentRepository shipmentRepository, OrderRepository orderRepository, OrderEventListener notificationService) {
        this.shipmentRepository  = shipmentRepository;
        this.orderRepository     = orderRepository;
        this.notificationService = notificationService;
    }

    public Shipment createShipmentForOrder(Order order) {
        Shipment shipment = new Shipment(order);
        return shipmentRepository.save(shipment);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Shipment> getByStatus(Shipment.ShipmentStatus status) {
        return shipmentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Shipment getById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId).orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));
    }

    @Transactional(readOnly = true)
    public Shipment getByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("Shipment for order " + orderId + " not found."));
    }

    public Shipment markPacked(Long shipmentId, String packageNotes) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.PENDING) {
            throw new IllegalStateException("Shipment must be PENDING to mark as packed.");
        }
        shipment.markPacked(packageNotes);
        orderRepository.save(shipment.getOrder());
        return shipmentRepository.save(shipment);
    }

    public Shipment markDispatched(Long shipmentId, String courierName, String trackingNumber, LocalDate estimatedDelivery) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.PACKED) {
            throw new IllegalStateException("Shipment must be PACKED before dispatch.");
        }
        if (courierName == null || courierName.isBlank()) {
            throw new IllegalArgumentException("Courier name is required.");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required.");
        }
        shipment.markDispatched(courierName, trackingNumber);
        shipment.setEstimatedDelivery(estimatedDelivery);
        orderRepository.save(shipment.getOrder());
        Shipment saved = shipmentRepository.save(shipment);

        notificationService.onShipmentDispatched(saved);
        return saved;
    }

    public Shipment markDelivered(Long shipmentId) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.DISPATCHED) {
            throw new IllegalStateException("Shipment must be DISPATCHED to mark as delivered.");
        }
        shipment.markDelivered();
        orderRepository.save(shipment.getOrder());
        Shipment saved = shipmentRepository.save(shipment);

        notificationService.onShipmentDelivered(saved);
        return saved;
    }
}
