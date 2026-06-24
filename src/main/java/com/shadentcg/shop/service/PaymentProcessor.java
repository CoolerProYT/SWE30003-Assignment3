package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Payment;
import com.shadentcg.shop.observer.OrderEventListener;
import com.shadentcg.shop.repository.OrderRepository;
import com.shadentcg.shop.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade over the external Payment Gateway (A2 class).
 *
 * <p><strong>A2 Design Pattern: Facade</strong>
 * PaymentProcessor presents a simplified, stable interface to the rest of the
 * design, shielding internal classes from payment gateway API contracts,
 * authentication, error codes, and retry logic.
 * Swapping payment providers (e.g. Stripe → PayPal) requires changes only
 * inside this class — satisfying the Open/Closed Principle.
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Submit payment requests to the payment gateway (simulated)</li>
 *   <li>Handle authorisation responses and failure scenarios</li>
 *   <li>Create a {@link Payment} data record on successful authorisation</li>
 *   <li>Notify {@link NotificationService} (Observer) of authorisation</li>
 * </ul>
 *
 * <p>Per the assignment spec, actual payment processing is simulated;
 * all payments are auto-authorised.
 */
@Service
@Transactional
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderEventListener notificationService;

    public PaymentProcessor(PaymentRepository paymentRepository,
                            OrderRepository orderRepository,
                            OrderEventListener notificationService) {
        this.paymentRepository   = paymentRepository;
        this.orderRepository     = orderRepository;
        this.notificationService = notificationService;
    }

    // ── Facade interface ──────────────────────────────────────────────

    /**
     * Submits a payment request to the gateway and records the result.
     *
     * <p>This method simulates authorisation. In production it would call
     * the real payment gateway API, handle 3DS challenges, timeouts, etc.
     *
     * @param order the order to pay for
     * @return the created {@link Payment} record
     * @throws IllegalStateException if payment has already been processed for this order
     */
    public Payment processPayment(Order order) {
        if (order.getPayment() != null) {
            throw new IllegalStateException(
                "Payment already processed for order #" + order.getId());
        }

        log.info("[PaymentProcessor] Submitting payment of ${} for order #{}",
                 order.getTotalAmount(), order.getId());

        // ── Simulate gateway call ────────────────────────────────────
        // In production: call gateway API, parse response, handle errors.
        // For this implementation, all payments are auto-authorised per spec.
        Payment payment = new Payment(order);
        order.setPayment(payment);
        order.updateStatus(Order.OrderStatus.CONFIRMED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("[PaymentProcessor] Payment authorised. Reference: {}",
                 payment.getPaymentReference());

        // Notify observer (NotificationService) — A2 Observer pattern
        notificationService.onPaymentAuthorised(order);

        return payment;
    }

    /**
     * Simulates a gateway failure response (for testing error scenarios).
     *
     * @param order the order whose payment failed
     */
    public void simulatePaymentFailure(Order order) {
        log.warn("[PaymentProcessor] Payment FAILED for order #{} (simulated gateway decline)",
                 order.getId());
        // In production: update payment record to FAILED, notify customer
    }
}
