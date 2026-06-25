package com.shadentcg.shop.observer;

import com.shadentcg.shop.model.Order;
import com.shadentcg.shop.model.Shipment;

public interface OrderEventListener {
    void onOrderConfirmed(Order order);
    void onPaymentAuthorised(Order order);
    void onShipmentDispatched(Shipment shipment);
    void onShipmentDelivered(Shipment shipment);
    void onOrderCancelled(Order order);
}
