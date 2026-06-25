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

@Service
@Transactional
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderEventListener notificationService;

    public PaymentProcessor(PaymentRepository paymentRepository, OrderRepository orderRepository, OrderEventListener notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    public Payment processPayment(Order order) {
        if (order.getPayment() != null) {
            throw new IllegalStateException("Payment already processed for order #" + order.getId());
        }

        log.info("[PaymentProcessor] Submitting payment of ${} for order #{}", order.getTotalAmount(), order.getId());

        Payment payment = new Payment(order);
        order.setPayment(payment);
        order.updateStatus(Order.OrderStatus.CONFIRMED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("[PaymentProcessor] Payment authorised. Reference: {}", payment.getPaymentReference());

        notificationService.onPaymentAuthorised(order);

        return payment;
    }


    public void simulatePaymentFailure(Order order) {
        log.warn("[PaymentProcessor] Payment FAILED for order #{} (simulated gateway decline)", order.getId());
    }
}
