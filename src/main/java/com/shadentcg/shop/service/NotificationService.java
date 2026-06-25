package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Shipment;
import com.shadentcg.shop.observer.OrderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Override
    public void onOrderConfirmed(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Confirmed – #" + order.getId();
        String body = String.format(
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

    @Override
    public void onPaymentAuthorised(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Payment Received – Order #" + order.getId();
        String body  = String.format(
            "Hi %s,%n%nPayment of $%.2f has been received for order #%d.%n" +
            "Reference: %s",
            order.getCustomer().getFullName(),
            order.getTotalAmount(),
            order.getId(),
            order.getPayment() != null ? order.getPayment().getPaymentReference() : "N/A"
        );
        sendEmail(to, subject, body);
    }

    @Override
    public void onShipmentDispatched(Shipment shipment) {
        String to = shipment.getOrder().getCustomer().getEmail();
        String subject = "Your Order Has Been Shipped – #" + shipment.getOrder().getId();
        String body = String.format(
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

    @Override
    public void onShipmentDelivered(Shipment shipment) {
        String to = shipment.getOrder().getCustomer().getEmail();
        String subject = "Order Delivered – #" + shipment.getOrder().getId();
        String body = String.format(
            "Hi %s,%n%nYour order #%d has been delivered. Enjoy!",
            shipment.getOrder().getCustomer().getFullName(),
            shipment.getOrder().getId()
        );
        sendEmail(to, subject, body);
    }

    @Override
    public void onOrderCancelled(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Cancelled – #" + order.getId();
        String body = String.format(
            "Hi %s,%n%nYour order #%d has been cancelled.",
            order.getCustomer().getFullName(),
            order.getId()
        );
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        log.info("[NotificationService] EMAIL TO: {} | SUBJECT: {} | BODY: {}", to, subject, body.replace("\n", " | "));
    }
}
