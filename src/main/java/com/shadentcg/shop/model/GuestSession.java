package com.shadentcg.shop.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class GuestSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sessionToken;
    private final ShoppingCart cart;
    private final LocalDateTime createdAt;

    public GuestSession() {
        this.sessionToken = UUID.randomUUID().toString();
        this.cart = new ShoppingCart();
        this.createdAt = LocalDateTime.now();
    }

    public void transferCartTo(ShoppingCart customerCart) {
        customerCart.mergeFrom(this.cart);
        this.cart.clear();
    }

    public boolean hasCartItems() {
        return !cart.isEmpty();
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public ShoppingCart getCart() {
        return cart;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
