package com.shadentcg.shop.config;

import com.shadentcg.shop.model.*;
import com.shadentcg.shop.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Seeds the database with an admin account, product categories, and sample
 * products on first startup (matches A2 bootstrap sequence steps 1-2).
 *
 * <p>Default credentials:
 * <ul>
 *   <li>Admin:    admin@shaden.com / admin123</li>
 *   <li>Customer: customer@shaden.com / customer123</li>
 * </ul>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customerRepository,
                      ProductRepository productRepository,
                      ProductCategoryRepository categoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder    = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedAdminAccount();
        seedSampleCustomer();
        seedProducts();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;
        String[][] cats = {
            {"Booster Packs",  "Individual booster packs from all TCG brands"},
            {"Starter Decks",  "Ready-to-play pre-constructed decks"},
            {"Special Sets",   "Elite trainer boxes, bundles, and collector sets"},
            {"Accessories",    "Sleeves, deck boxes, binders, playmats, and toploader"},
        };
        for (String[] c : cats) {
            categoryRepository.save(new ProductCategory(c[0], c[1]));
        }
        System.out.println("[DataSeeder] Product categories seeded.");
    }

    private void seedAdminAccount() {
        if (customerRepository.existsByEmail("admin@shaden.com")) return;
        Customer admin = new Customer("Admin Staff", "admin@shaden.com",
                                      passwordEncoder.encode("admin123"), "03-9999-0001");
        admin.setRole("ROLE_ADMIN");
        admin.addDeliveryAddress(
            new DeliveryAddress("123 Shop Street", "Hawthorn", "VIC", "3122", "Australia"));
        customerRepository.save(admin);
        System.out.println("[DataSeeder] Admin account: admin@shaden.com / admin123");
    }

    private void seedSampleCustomer() {
        if (customerRepository.existsByEmail("customer@shaden.com")) return;
        Customer customer = new Customer("Jane Smith", "customer@shaden.com",
                                         passwordEncoder.encode("customer123"), "0412-345-678");
        customer.addDeliveryAddress(
            new DeliveryAddress("45 Oak Avenue", "Richmond", "VIC", "3121", "Australia"));
        customerRepository.save(customer);
        System.out.println("[DataSeeder] Sample customer: customer@shaden.com / customer123");
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        // Map category name → entity
        Map<String, ProductCategory> cats = new java.util.HashMap<>();
        categoryRepository.findAll().forEach(c -> cats.put(c.getName(), c));

        Object[][] products = {
            {"Pokemon Booster Pack (Scarlet & Violet)",
             "10 random S&V series cards including one guaranteed rare.",
             new BigDecimal("14.99"), 50, "Booster Packs"},
            {"Magic: The Gathering Draft Booster",
             "15 cards for drafting from the latest expansion set.",
             new BigDecimal("6.99"), 80, "Booster Packs"},
            {"One Piece Card Game Booster",
             "12 cards from the Romance Dawn set.",
             new BigDecimal("5.99"), 100, "Booster Packs"},
            {"Yu-Gi-Oh! Structure Deck",
             "Ready-to-play 43-card deck with extra deck included.",
             new BigDecimal("12.99"), 30, "Starter Decks"},
            {"Pokemon Elite Trainer Box",
             "9 booster packs, 65 sleeves, dice, coin and more.",
             new BigDecimal("59.99"), 20, "Special Sets"},
            {"Magic: The Gathering Bundle",
             "8 draft boosters plus 40 basic lands and accessories.",
             new BigDecimal("44.99"), 15, "Special Sets"},
            {"Card Sleeves – Clear (100 pack)",
             "Standard size premium clear card sleeves, acid-free.",
             new BigDecimal("4.99"), 200, "Accessories"},
            {"Deck Box – Black",
             "Hard plastic deck box fits 100 sleeved cards.",
             new BigDecimal("7.49"), 120, "Accessories"},
            {"9-Pocket Binder (360 cards)",
             "Side-loading with 40 pages, fits 360 sleeved cards.",
             new BigDecimal("19.99"), 45, "Accessories"},
            {"Playmat – Standard TCG",
             "60x35cm neoprene mat, rolled with carry tube.",
             new BigDecimal("24.99"), 60, "Accessories"},
            {"Card Toploaders 25-Pack",
             "Rigid 3x4 inch toploaders for protecting valuable cards.",
             new BigDecimal("3.99"), 300, "Accessories"},
        };

        for (Object[] p : products) {
            ProductCategory cat = cats.get((String) p[4]);
            if (cat == null) continue;
            productRepository.save(new Product(
                (String) p[0], (String) p[1],
                (BigDecimal) p[2], (Integer) p[3], cat));
        }
        System.out.println("[DataSeeder] Sample products seeded.");
    }
}
