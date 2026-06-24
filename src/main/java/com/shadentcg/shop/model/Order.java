package com.shadentcg.shop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a confirmed purchase order (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold order items, total amount, and delivery address</li>
 *   <li>Maintain the order lifecycle status</li>
 *   <li>Own an {@link Invoice} (1:1) and optionally a {@link Payment} and {@link Shipment}</li>
 *   <li>Track placement and update timestamps</li>
 * </ul>
 *
 * <p>A2 relationships:
 * <ul>
 *   <li>CustomerAccount places 0..* Orders</li>
 *   <li>Order generates 1 Invoice</li>
 *   <li>Order has 0..1 Payment</li>
 *   <li>Order has 0..1 Shipment</li>
 * </ul>
 */
@Entity
@Table(name = "orders")
public class Order {

    /** A2 order lifecycle states. */
    public enum OrderStatus {
        PENDING,    // Placed, awaiting payment confirmation
        CONFIRMED,  // Payment acknowledged
        PACKED,     // Packaged and ready for dispatch
        SHIPPED,    // Handed to courier
        DELIVERED,  // Delivered to customer
        CANCELLED   // Cancelled before shipment
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    /** A2: Order generates 1 Invoice (cascade so invoice is saved with order). */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Invoice invoice;

    /** A2: Order has 0..1 Payment. */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Payment payment;

    /** A2: Order has 0..1 Shipment. */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Shipment shipment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    @Column
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────

    public Order() {}

    public Order(Customer customer, String deliveryAddress) {
        this.customer        = customer;
        this.deliveryAddress = deliveryAddress;
        this.placedAt        = LocalDateTime.now();
        this.status          = OrderStatus.PENDING;
    }

    // ── Business logic ────────────────────────────────────────────────

    /** Adds an item and sets the bidirectional back-reference. */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    /** Recalculates total from current items. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Updates status and records the update timestamp. */
    public void updateStatus(OrderStatus newStatus) {
        this.status    = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { updateStatus(status); }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", status=" + status + ", total=" + totalAmount + "}";
    }
}
