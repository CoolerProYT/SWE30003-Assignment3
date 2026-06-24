package com.shadentcg.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered customer (CustomerAccount in A2 design).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Maintain identity, authentication credentials, and contact details</li>
 *   <li>Own a ShoppingCart and one or more DeliveryAddresses</li>
 *   <li>Track order history</li>
 * </ul>
 *
 * <p>Coding standard: Google Java Style Guide
 * https://google.github.io/styleguide/javaguide.html
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Must be a valid phone number")
    @Column(nullable = false)
    private String phone;

    /** Either "ROLE_CUSTOMER" or "ROLE_ADMIN". */
    @Column(nullable = false)
    private String role = "ROLE_CUSTOMER";

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * A2: CustomerAccount owns 1..* DeliveryAddress (data-holder).
     * Cascade so addresses are persisted/removed with the customer.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────

    public Customer() {}

    public Customer(String fullName, String email, String password, String phone) {
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
    }

    // ── Business logic ────────────────────────────────────────────────

    /** Returns the customer's default (first) delivery address, or null. */
    public DeliveryAddress getDefaultAddress() {
        return deliveryAddresses.isEmpty() ? null : deliveryAddresses.get(0);
    }

    /** Adds a delivery address and sets back-reference. */
    public void addDeliveryAddress(DeliveryAddress address) {
        address.setCustomer(this);
        deliveryAddresses.add(address);
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<DeliveryAddress> getDeliveryAddresses() { return deliveryAddresses; }
    public void setDeliveryAddresses(List<DeliveryAddress> deliveryAddresses) {
        this.deliveryAddresses = deliveryAddresses;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }
}
