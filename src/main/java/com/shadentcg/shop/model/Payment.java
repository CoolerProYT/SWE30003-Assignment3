package com.shadentcg.shop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data-holder recording the outcome of a payment transaction (A2 data-holder class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold payment reference, amount, method, status, and timestamp</li>
 *   <li>Be created by {@link PaymentProcessor} after gateway authorisation</li>
 *   <li>Be associated with exactly one {@link Order}</li>
 * </ul>
 *
 * <p>A2 relationship: Order has 0..1 Payment.
 * Payment processing is simulated per the assignment spec (no real gateway).
 */
@Entity
@Table(name = "payments")
public class Payment {

    /** Payment outcomes. */
    public enum PaymentStatus {
        PENDING,
        AUTHORISED,
        FAILED,
        REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A2: Payment associated with exactly one Order. */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    /** Gateway-issued reference number (simulated). */
    @Column(nullable = false)
    private String paymentReference;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Payment method label, e.g. "Credit Card (simulated)". */
    @Column(nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    /** Human-readable note shown to the customer (e.g. simulated confirmation). */
    @Column
    private String note;

    // ── Constructors ──────────────────────────────────────────────────

    public Payment() {}

    /**
     * Creates a simulated successful payment record for an order.
     *
     * @param order the order being paid
     */
    public Payment(Order order) {
        this.order            = order;
        this.amount           = order.getTotalAmount();
        this.paymentMethod    = "Credit Card (simulated)";
        this.paymentReference = "PAY-" + System.currentTimeMillis();
        this.status           = PaymentStatus.AUTHORISED;
        this.processedAt      = LocalDateTime.now();
        this.note             = "Payment authorised (simulated). Reference: " + this.paymentReference;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "Payment{ref='" + paymentReference + "', status=" + status + "}";
    }
}
