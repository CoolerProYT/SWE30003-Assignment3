package com.shadentcg.shop.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks fulfilment and delivery of a paid order (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold courier name, tracking number, packaging notes, and status</li>
 *   <li>Be created by {@link ShipmentManager} when an order is confirmed</li>
 *   <li>Progress through PENDING → PACKED → DISPATCHED → DELIVERED</li>
 *   <li>Trigger notifications via {@link NotificationService} on dispatch</li>
 * </ul>
 *
 * <p>A2 relationship: Order has 0..1 Shipment.
 */
@Entity
@Table(name = "shipments")
public class Shipment {

    public enum ShipmentStatus {
        PENDING,
        PACKED,
        DISPATCHED,
        DELIVERED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column
    private String courierName;

    @Column
    private String trackingNumber;

    @Column
    private String packageNotes;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column
    private LocalDate estimatedDelivery;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime dispatchedAt;

    @Column
    private LocalDateTime deliveredAt;

    // ── Constructors ──────────────────────────────────────────────────

    public Shipment() {}

    public Shipment(Order order) {
        this.order           = order;
        this.deliveryAddress = order.getDeliveryAddress();
        this.createdAt       = LocalDateTime.now();
        this.status          = ShipmentStatus.PENDING;
    }

    // ── Business logic ────────────────────────────────────────────────

    public void markPacked(String packageNotes) {
        this.packageNotes = packageNotes;
        this.status       = ShipmentStatus.PACKED;
        this.order.updateStatus(Order.OrderStatus.PACKED);
    }

    public void markDispatched(String courierName, String trackingNumber) {
        this.courierName     = courierName;
        this.trackingNumber  = trackingNumber;
        this.status          = ShipmentStatus.DISPATCHED;
        this.dispatchedAt    = LocalDateTime.now();
        this.order.updateStatus(Order.OrderStatus.SHIPPED);
    }

    public void markDelivered() {
        this.status      = ShipmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.order.updateStatus(Order.OrderStatus.DELIVERED);
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getPackageNotes() { return packageNotes; }
    public void setPackageNotes(String packageNotes) { this.packageNotes = packageNotes; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public LocalDate getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDate estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
