package com.shadentcg.shop.controller;

import com.shadentcg.shop.model.Customer;
import com.shadentcg.shop.service.CustomerService;
import com.shadentcg.shop.service.OrderProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderProcessor orderProcessor;

    public CustomerController(CustomerService customerService, OrderProcessor orderProcessor) {
        this.customerService = customerService;
        this.orderProcessor  = orderProcessor;
    }

    private Customer current(Authentication auth) {
        return customerService.findByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String profilePage(Authentication auth, Model model) {
        Customer customer = current(auth);
        model.addAttribute("customer", customer);
        model.addAttribute("defaultAddress", customer.getDefaultAddress());
        model.addAttribute("orders", orderProcessor.getOrdersForCustomer(customer.getId()));
        return "auth/profile";
    }

    @PostMapping("/update")
    public String updateProfile(Authentication auth, @RequestParam String fullName, @RequestParam String phone, @RequestParam String street, @RequestParam String suburb, @RequestParam String state, @RequestParam String postcode, RedirectAttributes ra) {
        try {
            Customer c = current(auth);
            customerService.updateProfile(c.getId(), fullName, phone);
            customerService.updateDefaultAddress(c.getId(), street, suburb, state, postcode);
            ra.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication auth, @RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String confirmNewPassword, RedirectAttributes ra) {
        if (!newPassword.equals(confirmNewPassword)) {
            ra.addFlashAttribute("passwordError", "New passwords do not match.");
            return "redirect:/account";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("passwordError", "Password must be at least 6 characters.");
            return "redirect:/account";
        }
        try {
            customerService.changePassword(current(auth).getId(), currentPassword, newPassword);
            ra.addFlashAttribute("passwordSuccess", "Password changed successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/account";
    }
}
