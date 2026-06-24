package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Shipment;
import com.shadentcg.shop.observer.OrderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Internal facade over the email notification service (A2 class).
 *
 * <p><strong>A2 Design Patterns:</strong>
 * <ol>
 *   <li><strong>Facade</strong> — wraps the external Email API (simulated here with
 *       log output). All email dispatch is centralised here; swapping providers
 *       (e.g. SendGrid → AWS SES) requires changes only in this class.</li>
 *   <li><strong>Observer</strong> — implements {@link OrderEventListener}.
 *       Coordinator classes (OrderProcessor, PaymentProcessor, ShipmentManager)
 *       call this service after each lifecycle transition instead of dispatching
 *       emails themselves, making notification trigger points unambiguous and
 *       traceable without a runtime event bus.</li>
 * </ol>
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Dispatch order confirmation emails to customers</li>
 *   <li>Dispatch payment authorisation notifications</li>
 *   <li>Dispatch shipment dispatch and delivery notifications</li>
 *   <li>Dispatch order cancellation notifications</li>
 * </ul>
 *
 * <p>Per the assignment spec, actual email sending is simulated; messages are
 * logged at INFO level to demonstrate the correct trigger points.
 */
@Service
public class NotificationService implements OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // ── OrderEventListener (Observer) implementation ──────────────────

    /**
     * Sends an order confirmation email to the customer.
     * Triggered by {@link OrderProcessor} after successful checkout.
     *
     * @param order the confirmed order
     */
    @Override
    public void onOrderConfirmed(Order order) {
        String to      = order.getCustomer().getEmail();
        String subject = "Order Confirmed – #" + order.getId();
        String body    = String.format(
            "Hi %s,%n%nYour order #%d has been confirmed.%n" +
            "Total: $%.2f%nDelivery to: %s%n%n" +
            "Invoice: %s%n%nThank you for shopping at Shaden TCG!",
            order.getCustomer().getFullName(),
            order.getId(),
            order.getTotalAmount(),
            order.getDeliveryAddress(),
            order.getInvoice() != null ? order.getInvoice().getInvoiceNumber() : "N/A"
        );
        sendEmail(to, subject, body);
    }

    /**
     * Sends a payment authorisation notification.
     * Triggered by {@link PaymentProcessor} after gateway response.
     *
     * @param order the order whose payment was authorised
     */
    @Override
    public void onPaymentAuthorised(Order order) {
        String to      = order.getCustomer().getEmail();
        String subject = "Payment Received – Order #" + order.getId();
        String body    = String.format(
            "Hi %s,%n%nPayment of $%.2f has been received for order #%d.%n" +
            "Reference: %s",
            order.getCustomer().getFullName(),
            order.getTotalAmount(),
            order.getId(),
            order.getPayment() != null ? order.getPayment().getPaymentReference() : "N/A"
        );
        sendEmail(to, subject, body);
    }

    /**
     * Sends a shipment dispatch notification with tracking details.
     * Triggered by {@link ShipmentManager} when a shipment is dispatched.
     *
     * @param shipment the dispatched shipment
     */
    @Override
    public void onShipmentDispatched(Shipment shipment) {
        String to      = shipment.getOrder().getCustomer().getEmail();
        String subject = "Your Order Has Been Shipped – #" + shipment.getOrder().getId();
        String body    = String.format(
            "Hi %s,%n%nYour order #%d has been dispatched!%n" +
            "Courier: %s%nTracking: %s%nEstimated delivery: %s",
            shipment.getOrder().getCustomer().getFullName(),
            shipment.getOrder().getId(),
            shipment.getCourierName(),
            shipment.getTrackingNumber(),
            shipment.getEstimatedDelivery() != null ? shipment.getEstimatedDelivery() : "TBC"
        );
        sendEmail(to, subject, body);
    }

    /**
     * Sends a delivery confirmation notification.
     * Triggered by {@link ShipmentManager} when delivery is confirmed.
     *
     * @param shipment the delivered shipment
     */
    @Override
    public void onShipmentDelivered(Shipment shipment) {
        String to      = shipment.getOrder().getCustomer().getEmail();
        String subject = "Order Delivered – #" + shipment.getOrder().getId();
        String body    = String.format(
            "Hi %s,%n%nYour order #%d has been delivered. Enjoy!",
            shipment.getOrder().getCustomer().getFullName(),
            shipment.getOrder().getId()
        );
        sendEmail(to, subject, body);
    }

    /**
     * Sends an order cancellation notification.
     * Triggered by {@link OrderProcessor} on cancellation.
     *
     * @param order the cancelled order
     */
    @Override
    public void onOrderCancelled(Order order) {
        String to      = order.getCustomer().getEmail();
        String subject = "Order Cancelled – #" + order.getId();
        String body    = String.format(
            "Hi %s,%n%nYour order #%d has been cancelled.",
            order.getCustomer().getFullName(),
            order.getId()
        );
        sendEmail(to, subject, body);
    }

    // ── Facade: internal email dispatch (simulated) ───────────────────

    /**
     * Dispatches an email via the Email API facade (simulated).
     *
     * <p>In production this would call the real Email Service API (e.g. SendGrid).
     * Logged at INFO level as evidence of the correct trigger points.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    email body text
     */
    private void sendEmail(String to, String subject, String body) {
        log.info("[NotificationService] EMAIL TO: {} | SUBJECT: {} | BODY: {}",
                 to, subject, body.replace("\n", " | "));
    }
}
