package com.shadentcg.shop.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an unauthenticated browsing session (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold a temporary {@link ShoppingCart} for a guest visitor</li>
 *   <li>Track session creation time and a unique session token</li>
 *   <li>Transfer cart contents to a {@link Customer} on registration or login</li>
 * </ul>
 *
 * <p>A2 design note: GuestSession cleanly separates unauthenticated browsing
 * from authenticated customer state, so Product and ShoppingCart do not need
 * to be aware of whether the user is logged in.
 *
 * <p>Stored in the HTTP session; not persisted to DB.
 */
public class GuestSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique token identifying this guest session. */
    private final String sessionToken;

    /** The guest's temporary shopping cart. */
    private final ShoppingCart cart;

    private final LocalDateTime createdAt;

    // ── Constructor ───────────────────────────────────────────────────

    public GuestSession() {
        this.sessionToken = UUID.randomUUID().toString();
        this.cart         = new ShoppingCart();
        this.createdAt    = LocalDateTime.now();
    }

    // ── Business logic ────────────────────────────────────────────────

    /**
     * Transfers this guest's cart contents into the given customer's cart.
     * Called when a guest registers or logs in.
     *
     * @param customerCart the authenticated customer's ShoppingCart to merge into
     */
    public void transferCartTo(ShoppingCart customerCart) {
        customerCart.mergeFrom(this.cart);
        this.cart.clear();
    }

    /** Returns true if the guest has added any items to their cart. */
    public boolean hasCartItems() {
        return !cart.isEmpty();
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String getSessionToken() { return sessionToken; }

    public ShoppingCart getCart() { return cart; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
