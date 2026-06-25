package com.shadentcg.shop.service;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.observer.OrderEventListener;
import com.shadentcg.shop.repository.InvoiceRepository;
import com.shadentcg.shop.repository.OrderRepository;
import com.shadentcg.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProcessor paymentProcessor;
    private final ShipmentManager shipmentManager;
    private final OrderEventListener notificationService;

    public OrderProcessor(OrderRepository orderRepository, ProductRepository productRepository, InvoiceRepository invoiceRepository, PaymentProcessor paymentProcessor, ShipmentManager shipmentManager, OrderEventListener notificationService) {
        this.orderRepository      = orderRepository;
        this.productRepository    = productRepository;
        this.invoiceRepository    = invoiceRepository;
        this.paymentProcessor     = paymentProcessor;
        this.shipmentManager      = shipmentManager;
        this.notificationService  = notificationService;
    }

    public Order checkout(Customer customer, ShoppingCart cart, String deliveryAddress) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart.");
        }

        Order order = new Order(customer, deliveryAddress);

        for (CartItem cartItem : cart.getItemList()) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(() -> new IllegalStateException("Product no longer exists: " + cartItem.getProductName()));
            product.deductStock(cartItem.getQuantity());
            productRepository.save(product);
            order.addItem(new OrderItem(product, cartItem.getQuantity()));
        }

        order.recalculateTotal();
        Order savedOrder = orderRepository.save(order);

        Invoice invoice = new Invoice(savedOrder);
        invoice = invoiceRepository.save(invoice);
        savedOrder.setInvoice(invoice);
        savedOrder = orderRepository.save(savedOrder);

        paymentProcessor.processPayment(savedOrder);
        shipmentManager.createShipmentForOrder(savedOrder);
        notificationService.onOrderConfirmed(savedOrder);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Order getById(Long orderId) {
        return orderRepository.findByIdWithCustomer(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll().stream().sorted((a, b) -> b.getPlacedAt().compareTo(a.getPlacedAt())).toList();
    }

    public Order cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has already shipped.");
        }

        for (OrderItem item : order.getItems()) {
            item.getProduct().restoreStock(item.getQuantity());
            productRepository.save(item.getProduct());
        }
        order.updateStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        notificationService.onOrderCancelled(saved);
        return saved;
    }
}
