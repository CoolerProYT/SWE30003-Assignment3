package com.shadentcg.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Shaden TCG Shop application.
 *
 * <p>Implements four business areas from Assignment 1:
 * <ul>
 *   <li>Customer account management</li>
 *   <li>Product catalogue management</li>
 *   <li>Shopping cart and order placement</li>
 *   <li>Shipment and packaging management</li>
 * </ul>
 *
 * <p>Class design matches the 18-class model from Assignment 2, including
 * all design patterns: Facade (PaymentProcessor, NotificationService),
 * Creator/Factory Method (OrderProcessor), and Observer (NotificationService).
 *
 * <p>Coding standard: Google Java Style Guide
 * https://google.github.io/styleguide/javaguide.html
 *
 * @author Group 9 – SWE30003 Semester 2, 2025
 * @version 1.0.0
 */
@SpringBootApplication
public class ShadenShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShadenShopApplication.class, args);
    }
}
