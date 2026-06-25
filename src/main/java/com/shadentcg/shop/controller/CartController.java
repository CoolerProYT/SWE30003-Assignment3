package com.shadentcg.shop.controller;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {
    private static final String SESSION_CART  = "cart";
    private static final String SESSION_GUEST = "guestSession";

    private final ProductCatalogue productCatalogue;
    private final CustomerService customerService;
    private final OrderProcessor orderProcessor;

    public CartController(ProductCatalogue productCatalogue, CustomerService customerService, OrderProcessor orderProcessor) {
        this.productCatalogue = productCatalogue;
        this.customerService  = customerService;
        this.orderProcessor   = orderProcessor;
    }

    private ShoppingCart getCart(HttpSession session, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            ShoppingCart cart = (ShoppingCart) session.getAttribute(SESSION_CART);
            if (cart == null) {
                cart = new ShoppingCart();
                session.setAttribute(SESSION_CART, cart);
            }
            return cart;
        }

        GuestSession guest = (GuestSession) session.getAttribute(SESSION_GUEST);
        if (guest == null) {
            guest = new GuestSession();
            session.setAttribute(SESSION_GUEST, guest);
        }
        return guest.getCart();
    }

    @GetMapping
    public String view(HttpSession session, Authentication auth, Model model) {
        ShoppingCart cart = getCart(session, auth);
        model.addAttribute("cart",      cart);
        model.addAttribute("cartItems", cart.getItemList());
        return "cart/view";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session, Authentication auth, RedirectAttributes ra) {
        try {
            Product p = productCatalogue.getProductById(productId);
            if (!p.hasStock(quantity)) {
                ra.addFlashAttribute("cartError", "Not enough stock for: " + p.getName());
                return "redirect:/catalogue";
            }
            getCart(session, auth).addItem(p.getId(), p.getName(), p.getPrice(), quantity);
            ra.addFlashAttribute("cartSuccess", "Added to cart: " + p.getName());
        } catch (Exception e) { ra.addFlashAttribute("cartError", e.getMessage()); }
        return "redirect:/catalogue";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long productId, @RequestParam int quantity, HttpSession session, Authentication auth) {
        getCart(session, auth).updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId, HttpSession session, Authentication auth) {
        getCart(session, auth).removeItem(productId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Authentication auth, HttpSession session, Model model) {
        ShoppingCart cart = getCart(session, auth);
        if (cart.isEmpty()) return "redirect:/cart";
        Customer customer = customerService.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getItemList());
        model.addAttribute("customer", customer);
        model.addAttribute("defaultAddress", customer.getDefaultAddress());
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(Authentication auth, HttpSession session, @RequestParam String deliveryAddress, RedirectAttributes ra) {
        ShoppingCart cart = getCart(session, auth);
        if (cart.isEmpty()) return "redirect:/cart";
        try {
            Customer customer = customerService.findByEmail(auth.getName()).orElseThrow();
            Order order = orderProcessor.checkout(customer, cart, deliveryAddress);
            cart.clear();
            return "redirect:/orders/" + order.getId() + "/confirmation";
        } catch (Exception e) {
            ra.addFlashAttribute("checkoutError", e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
