package com.shadentcg.shop.observer;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Shipment;

/**
 * Observer interface for order and shipment lifecycle events (A2 Observer pattern).
 *
 * <p>A2 design pattern: Observer (lightweight, via NotificationService).
 * When significant state transitions occur, coordinator classes
 * (OrderProcessor, PaymentProcessor, ShipmentManager) call methods on this
 * interface. NotificationService is the concrete observer.
 *
 * <p>This avoids scattered notification logic across multiple classes.
 */
public interface OrderEventListener {

    /**
     * Called when a new order has been successfully placed and confirmed.
     *
     * @param order the confirmed order
     */
    void onOrderConfirmed(Order order);

    /**
     * Called when payment for an order has been authorised.
     *
     * @param order the order whose payment was authorised
     */
    void onPaymentAuthorised(Order order);

    /**
     * Called when a shipment has been dispatched to the courier.
     *
     * @param shipment the dispatched shipment
     */
    void onShipmentDispatched(Shipment shipment);

    /**
     * Called when a shipment has been confirmed as delivered.
     *
     * @param shipment the delivered shipment
     */
    void onShipmentDelivered(Shipment shipment);

    /**
     * Called when an order has been cancelled.
     *
     * @param order the cancelled order
     */
    void onOrderCancelled(Order order);
}
