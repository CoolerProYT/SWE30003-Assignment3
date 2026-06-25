package com.shadentcg.shop.controller;

import com.shadentcg.shop.model.GuestSession;
import com.shadentcg.shop.model.ShoppingCart;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private static final String SESSION_CART  = "cart";
    private static final String SESSION_GUEST = "guestSession";

    @ModelAttribute("cart")
    public ShoppingCart cart(HttpSession session, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return (ShoppingCart) session.getAttribute(SESSION_CART);
        }
        GuestSession guest = (GuestSession) session.getAttribute(SESSION_GUEST);
        return guest != null ? guest.getCart() : null;
    }
}
