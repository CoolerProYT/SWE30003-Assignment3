package com.shadentcg.shop.controller;

import com.shadentcg.shop.service.ProductCatalogue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
public class CatalogueController {
    private final ProductCatalogue productCatalogue;

    public CatalogueController(ProductCatalogue productCatalogue) {
        this.productCatalogue = productCatalogue;
    }

    @GetMapping({"/", "/catalogue"})
    public String catalogue(@RequestParam(required = false) String keyword, @RequestParam(required = false) Long category, Model model) {
        model.addAttribute("products", category != null ? productCatalogue.getByCategory(category) : productCatalogue.search(keyword));
        model.addAttribute("categories",   productCatalogue.getAllCategories());
        model.addAttribute("keyword",      keyword);
        model.addAttribute("selectedCategory", category);
        return "catalogue/index";
    }

    @GetMapping("/catalogue/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productCatalogue.getProductById(id));
        return "catalogue/detail";
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute("products", productCatalogue.getAllForAdmin());
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String newForm(Model model) {
        model.addAttribute("categories", productCatalogue.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/admin/products/new")
    public String create(@RequestParam String name, @RequestParam String description, @RequestParam BigDecimal price, @RequestParam int stockQuantity, @RequestParam Long categoryId, RedirectAttributes ra) {
        try {
            productCatalogue.createProduct(name, description, price, stockQuantity, categoryId);
            ra.addFlashAttribute("successMessage", "Product created.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product",    productCatalogue.getProductById(id));
        model.addAttribute("categories", productCatalogue.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam String name, @RequestParam String description, @RequestParam BigDecimal price, @RequestParam int stockQuantity, @RequestParam Long categoryId, RedirectAttributes ra) {
        try {
            productCatalogue.updateProduct(id, name, description, price, stockQuantity, categoryId);
            ra.addFlashAttribute("successMessage", "Product updated.");
        } catch (Exception e) { ra.addFlashAttribute("errorMessage", e.getMessage()); }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        productCatalogue.deactivateProduct(id);
        ra.addFlashAttribute("successMessage", "Product deactivated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/reactivate")
    public String reactivate(@PathVariable Long id, RedirectAttributes ra) {
        productCatalogue.reactivateProduct(id);
        ra.addFlashAttribute("successMessage", "Product reactivated.");
        return "redirect:/admin/products";
    }
}
