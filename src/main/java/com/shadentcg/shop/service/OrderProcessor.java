package com.shadentcg.shop.service;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.observer.OrderEventListener;
import com.shadentcg.shop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application-service coordinator for the checkout and order lifecycle (A2 class).
 *
 * <p><strong>A2 Design Pattern: Creator / Factory Method</strong>
 * OrderProcessor is the sole creator of {@link Order} and {@link Invoice} objects.
 * No other class creates these directly. This centralises creation logic and
 * ensures all pre-conditions (stock reservation, cart validation, address check)
 * are enforced before an Order or Invoice comes into existence.
 * This follows the RDD Creator heuristic: a class should create instances of
 * another if it aggregates or uses those instances closely, or holds the
 * initialisation data. OrderProcessor satisfies all three criteria.
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Orchestrate checkout: validate cart, reserve stock, create Order and Invoice</li>
 *   <li>Delegate payment to {@link PaymentProcessor}</li>
 *   <li>Delegate shipment creation to {@link ShipmentManager}</li>
 *   <li>Notify {@link NotificationService} on order confirmation and cancellation</li>
 *   <li>Handle order cancellation and stock restoration</li>
 * </ul>
 */
@Service
@Transactional
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProcessor paymentProcessor;
    private final ShipmentManager shipmentManager;
    private final OrderEventListener notificationService;

    public OrderProcessor(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          InvoiceRepository invoiceRepository,
                          PaymentProcessor paymentProcessor,
                          ShipmentManager shipmentManager,
                          OrderEventListener notificationService) {
        this.orderRepository      = orderRepository;
        this.productRepository    = productRepository;
        this.invoiceRepository    = invoiceRepository;
        this.paymentProcessor     = paymentProcessor;
        this.shipmentManager      = shipmentManager;
        this.notificationService  = notificationService;
    }

    // ── Checkout ──────────────────────────────────────────────────────

    /**
     * Converts a session shopping cart into a fully persisted Order.
     *
     * <p>Workflow (matches A2 bootstrap sequence steps 9–11):
     * <ol>
     *   <li>Validate cart is non-empty</li>
     *   <li>Validate and deduct stock for each item (atomic)</li>
     *   <li><strong>Create Order</strong> (Creator pattern)</li>
     *   <li><strong>Create Invoice</strong> (Creator pattern)</li>
     *   <li>Delegate payment to PaymentProcessor</li>
     *   <li>Delegate shipment creation to ShipmentManager</li>
     *   <li>Notify NotificationService (Observer)</li>
     * </ol>
     *
     * @param customer        the purchasing customer
     * @param cart            the session shopping cart
     * @param deliveryAddress the delivery address for this order
     * @return the persisted {@link Order} with Invoice and Payment attached
     * @throws IllegalArgumentException if the cart is empty
     * @throws IllegalStateException    if any product has insufficient stock
     */
    public Order checkout(Customer customer, ShoppingCart cart, String deliveryAddress) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart.");
        }

        // Step 1: Create Order
        Order order = new Order(customer, deliveryAddress);

        // Step 2: Validate stock and deduct, build order items
        for (CartItem cartItem : cart.getItemList()) {
            Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new IllegalStateException(
                    "Product no longer exists: " + cartItem.getProductName()));
            product.deductStock(cartItem.getQuantity());
            productRepository.save(product);
            order.addItem(new OrderItem(product, cartItem.getQuantity()));
        }

        order.recalculateTotal();
        Order savedOrder = orderRepository.save(order);

        // Step 3: Create Invoice — OrderProcessor is the sole creator (Creator pattern)
        Invoice invoice = new Invoice(savedOrder);
        invoice = invoiceRepository.save(invoice);
        savedOrder.setInvoice(invoice);
        savedOrder = orderRepository.save(savedOrder);

        // Step 4: Process payment via PaymentProcessor facade
        paymentProcessor.processPayment(savedOrder);

        // Step 5: Create shipment via ShipmentManager
        shipmentManager.createShipmentForOrder(savedOrder);

        // Step 6: Notify customer — Observer pattern
        notificationService.onOrderConfirmed(savedOrder);

        return savedOrder;
    }

    // ── Order retrieval ───────────────────────────────────────────────

    /** Returns all orders for a given customer, most recent first. */
    @Transactional(readOnly = true)
    public List<Order> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId);
    }

    /** Returns a specific order by ID. */
    @Transactional(readOnly = true)
    public Order getById(Long orderId) {
        return orderRepository.findByIdWithCustomer(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    /** Returns all orders (admin use), most recent first. */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll()
            .stream()
            .sorted((a, b) -> b.getPlacedAt().compareTo(a.getPlacedAt()))
            .toList();
    }

    // ── Order lifecycle ───────────────────────────────────────────────

    /**
     * Cancels an order and restores product stock.
     *
     * @param orderId the order to cancel
     * @return the updated Order
     * @throws IllegalStateException if the order has already shipped or been delivered
     */
    public Order cancelOrder(Long orderId) {
        Order order = getById(orderId);
        if (order.getStatus() == Order.OrderStatus.SHIPPED
            || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                "Cannot cancel an order that has already shipped.");
        }
        // Restore stock for each item
        for (OrderItem item : order.getItems()) {
            item.getProduct().restoreStock(item.getQuantity());
            productRepository.save(item.getProduct());
        }
        order.updateStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // Notify customer — Observer pattern
        notificationService.onOrderCancelled(saved);
        return saved;
    }
}
