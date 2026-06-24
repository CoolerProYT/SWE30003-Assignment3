package com.shadentcg.shop.service;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.observer.OrderEventListener;
import com.shadentcg.shop.repository.OrderRepository;
import com.shadentcg.shop.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages the fulfilment queue and dispatching workflow (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Create {@link Shipment} records for confirmed orders</li>
 *   <li>Progress shipments: PENDING → PACKED → DISPATCHED → DELIVERED</li>
 *   <li>Notify {@link NotificationService} (Observer) on dispatch and delivery</li>
 * </ul>
 *
 * <p>Used by admin (Delivery Officer) and called by {@link OrderProcessor}
 * during checkout to create the initial shipment record.
 */
@Service
@Transactional
public class ShipmentManager {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderEventListener notificationService;

    public ShipmentManager(ShipmentRepository shipmentRepository,
                           OrderRepository orderRepository,
                           OrderEventListener notificationService) {
        this.shipmentRepository  = shipmentRepository;
        this.orderRepository     = orderRepository;
        this.notificationService = notificationService;
    }

    // ── Creation ──────────────────────────────────────────────────────

    /**
     * Creates a pending {@link Shipment} for a newly confirmed order.
     * Called by {@link OrderProcessor} as part of the checkout workflow.
     *
     * @param order the confirmed order
     * @return the persisted Shipment
     */
    public Shipment createShipmentForOrder(Order order) {
        Shipment shipment = new Shipment(order);
        return shipmentRepository.save(shipment);
    }

    // ── Retrieval ─────────────────────────────────────────────────────

    /** Returns all shipments, most recently created first. */
    @Transactional(readOnly = true)
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Returns shipments in the given status. */
    @Transactional(readOnly = true)
    public List<Shipment> getByStatus(Shipment.ShipmentStatus status) {
        return shipmentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /** Returns a shipment by its own ID. */
    @Transactional(readOnly = true)
    public Shipment getById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Shipment not found: " + shipmentId));
    }

    /** Returns the shipment associated with a given order. */
    @Transactional(readOnly = true)
    public Shipment getByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Shipment for order " + orderId + " not found."));
    }

    // ── Status transitions ────────────────────────────────────────────

    /**
     * Marks a shipment as packed with optional packaging notes.
     *
     * @param shipmentId   shipment to update
     * @param packageNotes optional packaging notes
     * @return updated Shipment
     * @throws IllegalStateException if not in PENDING status
     */
    public Shipment markPacked(Long shipmentId, String packageNotes) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.PENDING) {
            throw new IllegalStateException(
                "Shipment must be PENDING to mark as packed.");
        }
        shipment.markPacked(packageNotes);
        orderRepository.save(shipment.getOrder());
        return shipmentRepository.save(shipment);
    }

    /**
     * Marks a shipment as dispatched to the courier.
     * Triggers notification to customer via Observer pattern.
     *
     * @param shipmentId       shipment to dispatch
     * @param courierName      courier company name
     * @param trackingNumber   courier tracking reference
     * @param estimatedDelivery estimated delivery date (optional)
     * @return updated Shipment
     * @throws IllegalStateException    if not in PACKED status
     * @throws IllegalArgumentException if courier or tracking details are blank
     */
    public Shipment markDispatched(Long shipmentId, String courierName,
                                   String trackingNumber, LocalDate estimatedDelivery) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.PACKED) {
            throw new IllegalStateException(
                "Shipment must be PACKED before dispatch.");
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

        // Notify customer — Observer pattern
        notificationService.onShipmentDispatched(saved);
        return saved;
    }

    /**
     * Marks a shipment as delivered.
     * Triggers notification to customer via Observer pattern.
     *
     * @param shipmentId shipment ID
     * @return updated Shipment
     * @throws IllegalStateException if not in DISPATCHED status
     */
    public Shipment markDelivered(Long shipmentId) {
        Shipment shipment = getById(shipmentId);
        if (shipment.getStatus() != Shipment.ShipmentStatus.DISPATCHED) {
            throw new IllegalStateException(
                "Shipment must be DISPATCHED to mark as delivered.");
        }
        shipment.markDelivered();
        orderRepository.save(shipment.getOrder());
        Shipment saved = shipmentRepository.save(shipment);

        // Notify customer — Observer pattern
        notificationService.onShipmentDelivered(saved);
        return saved;
    }
}
