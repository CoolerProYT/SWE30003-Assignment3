package com.shadentcg.shop.controller;

import com.shadentcg.shop.model.GuestSession;
import com.shadentcg.shop.model.ShoppingCart;
import com.shadentcg.shop.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final String SESSION_GUEST = "guestSession";
    private static final String SESSION_CART  = "cart";

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, org.springframework.ui.Model model) {
        if (error != null)  model.addAttribute("loginError",   "Invalid email or password.");
        if (logout != null) model.addAttribute("logoutMessage","You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName, @RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword, @RequestParam String phone, @RequestParam String street, @RequestParam String suburb, @RequestParam String state, @RequestParam String postcode, HttpSession session, RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("registerError", "Passwords do not match.");
            return "redirect:/auth/register";
        }
        try {
            customerService.register(fullName, email, password, phone, street, suburb, state, postcode);

            GuestSession guest = (GuestSession) session.getAttribute(SESSION_GUEST);
            if (guest != null && guest.hasCartItems()) {
                ShoppingCart cart = new ShoppingCart();
                guest.transferCartTo(cart);
                session.setAttribute(SESSION_CART, cart);
                session.removeAttribute(SESSION_GUEST);
            }

            ra.addFlashAttribute("registerSuccess", "Account created! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("registerError", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
