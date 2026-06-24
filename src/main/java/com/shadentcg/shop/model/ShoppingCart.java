package com.shadentcg.shop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Session-scoped shopping cart holding a customer's selected items (A2 class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Maintain the active collection of {@link CartItem}s the customer intends to buy</li>
 *   <li>Add, update, and remove items</li>
 *   <li>Calculate the cart total and item count</li>
 *   <li>Clear itself after successful checkout</li>
 * </ul>
 *
 * <p>A2: CustomerAccount owns 1 ShoppingCart; GuestSession owns 1 temporary ShoppingCart.
 * Stored in the HTTP session; not persisted to DB.
 */
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Items keyed by productId for O(1) lookup. */
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    // ── Cart operations ───────────────────────────────────────────────

    /**
     * Adds a product to the cart. If already present, increments quantity.
     *
     * @param productId   product DB id
     * @param productName name snapshot at add-time
     * @param unitPrice   price snapshot at add-time
     * @param quantity    units to add (must be ≥ 1)
     * @throws IllegalArgumentException if quantity is less than 1
     */
    public void addItem(Long productId, String productName,
                        BigDecimal unitPrice, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        CartItem existing = items.get(productId);
        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            items.put(productId, new CartItem(productId, productName, unitPrice, quantity));
        }
    }

    /**
     * Updates the quantity of an existing item. Removes it if newQty ≤ 0.
     *
     * @param productId product to update
     * @param newQty    desired quantity
     */
    public void updateQuantity(Long productId, int newQty) {
        if (newQty <= 0) {
            items.remove(productId);
        } else {
            CartItem item = items.get(productId);
            if (item != null) item.setQuantity(newQty);
        }
    }

    /** Removes a product entirely from the cart. */
    public void removeItem(Long productId) {
        items.remove(productId);
    }

    /** Empties the cart. Called after successful order placement. */
    public void clear() {
        items.clear();
    }

    /** Merges another cart's items into this one (used on guest→account transfer). */
    public void mergeFrom(ShoppingCart other) {
        for (CartItem item : other.getItemList()) {
            addItem(item.getProductId(), item.getProductName(),
                    item.getUnitPrice(), item.getQuantity());
        }
    }

    // ── Computed properties ───────────────────────────────────────────

    /** Grand total of all line subtotals. */
    public BigDecimal getTotal() {
        return items.values().stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Sum of all item quantities (for the nav badge). */
    public int getTotalItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    /** Returns true if the cart holds no items. */
    public boolean isEmpty() { return items.isEmpty(); }

    /** Ordered list of cart items for display. */
    public List<CartItem> getItemList() { return new ArrayList<>(items.values()); }

    /** Unmodifiable view of the underlying map. */
    public Map<Long, CartItem> getItems() { return Collections.unmodifiableMap(items); }
}
