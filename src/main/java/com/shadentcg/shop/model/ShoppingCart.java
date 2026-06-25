package com.shadentcg.shop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void addItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
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

    public void updateQuantity(Long productId, int newQty) {
        if (newQty <= 0) {
            items.remove(productId);
        } else {
            CartItem item = items.get(productId);
            if (item != null) item.setQuantity(newQty);
        }
    }

    public void removeItem(Long productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public void mergeFrom(ShoppingCart other) {
        for (CartItem item : other.getItemList()) {
            addItem(item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity());
        }
    }

    public BigDecimal getTotal() {
        return items.values().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public List<CartItem> getItemList() { return new ArrayList<>(items.values()); }

    public Map<Long, CartItem> getItems() { return Collections.unmodifiableMap(items); }
}
