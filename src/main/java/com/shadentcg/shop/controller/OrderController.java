package com.shadentcg.shop.controller;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class OrderController {
    private final OrderProcessor orderProcessor;
    private final CustomerService customerService;
    private final ShipmentManager shipmentManager;
    private final SalesReportGenerator salesReportGenerator;

    public OrderController(OrderProcessor orderProcessor, CustomerService customerService, ShipmentManager shipmentManager, SalesReportGenerator salesReportGenerator) {
        this.orderProcessor = orderProcessor;
        this.customerService = customerService;
        this.shipmentManager = shipmentManager;
        this.salesReportGenerator = salesReportGenerator;
    }

    @GetMapping("/orders")
    public String history(Authentication auth, Model model) {
        Customer c = customerService.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("orders", orderProcessor.getOrdersForCustomer(c.getId()));
        return "cart/orders";
    }

    @GetMapping("/orders/{id}/confirmation")
    public String confirmation(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderProcessor.getById(id));
        return "cart/confirmation";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        Order order = orderProcessor.getById(id);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getCustomer().getEmail().equals(auth.getName()))
            return "redirect:/orders";
        model.addAttribute("order", order);
        try {
            model.addAttribute("shipment", shipmentManager.getByOrderId(id));
        } catch (IllegalArgumentException ignored) {
        }
        return "cart/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderProcessor.cancelOrder(id);
            ra.addFlashAttribute("successMessage", "Order #" + id + " cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        model.addAttribute("orders", orderProcessor.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/admin/shipments")
    public String adminShipments(Model model) {
        model.addAttribute("shipments", shipmentManager.getAllShipments());
        return "shipment/admin-list";
    }

    @GetMapping("/admin/shipments/{id}")
    public String shipmentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("shipment", shipmentManager.getById(id));
        return "shipment/detail";
    }

    @PostMapping("/admin/shipments/{id}/pack")
    public String pack(@PathVariable Long id, @RequestParam(required = false) String packageNotes, RedirectAttributes ra) {
        try {
            shipmentManager.markPacked(id, packageNotes);
            ra.addFlashAttribute("successMessage", "Marked as packed.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipments/" + id;
    }

    @PostMapping("/admin/shipments/{id}/dispatch")
    public String dispatch(@PathVariable Long id,
                           @RequestParam String courierName,
                           @RequestParam String trackingNumber,
                           @RequestParam(required = false) LocalDate estimatedDelivery,
                           RedirectAttributes ra) {
        try {
            shipmentManager.markDispatched(id, courierName, trackingNumber, estimatedDelivery);
            ra.addFlashAttribute("successMessage", "Shipment dispatched.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipments/" + id;
    }

    @PostMapping("/admin/shipments/{id}/deliver")
    public String deliver(@PathVariable Long id, RedirectAttributes ra) {
        try {
            shipmentManager.markDelivered(id);
            ra.addFlashAttribute("successMessage", "Marked as delivered.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipments/" + id;
    }

    // ── Admin sales report ─────────────────────────────────────────────

    @GetMapping("/admin/reports")
    public String salesReport(@RequestParam(required = false) LocalDate start,
                              @RequestParam(required = false) LocalDate end,
                              Model model) {
        model.addAttribute("report",
            (start != null && end != null)
                ? salesReportGenerator.generateCustomReport(start, end)
                : salesReportGenerator.generateMonthlyReport());
        return "admin/report";
    }
}
