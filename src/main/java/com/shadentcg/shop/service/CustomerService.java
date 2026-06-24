package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Customer;
import com.shadentcg.shop.model.DeliveryAddress;
import com.shadentcg.shop.model.GuestSession;
import com.shadentcg.shop.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for customer account management operations.
 *
 * <p>Handles registration (including GuestSession cart transfer on login),
 * profile retrieval and update, and password changes.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder    = passwordEncoder;
    }

    // ── Registration ──────────────────────────────────────────────────

    /**
     * Registers a new customer account with one default delivery address.
     * If a GuestSession with cart items exists it should be transferred
     * by the controller after calling this method.
     *
     * @param fullName  customer's full name
     * @param email     unique email address
     * @param rawPassword plain-text password
     * @param phone     phone number
     * @param street    street address
     * @param suburb    suburb
     * @param state     state
     * @param postcode  postcode
     * @return the persisted Customer
     * @throws IllegalArgumentException if the email is already registered
     */
    public Customer register(String fullName, String email, String rawPassword,
                             String phone, String street, String suburb,
                             String state, String postcode) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "An account with email '" + email + "' already exists.");
        }
        Customer customer = new Customer(fullName, email,
                                         passwordEncoder.encode(rawPassword), phone);
        DeliveryAddress address = new DeliveryAddress(street, suburb, state, postcode, "Australia");
        address.setDefault(true);
        customer.addDeliveryAddress(address);
        return customerRepository.save(customer);
    }

    // ── Retrieval ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    // ── Profile update ────────────────────────────────────────────────

    /**
     * Updates a customer's basic profile fields.
     *
     * @param id      customer ID
     * @param fullName updated name
     * @param phone   updated phone
     * @return updated Customer
     */
    public Customer updateProfile(Long id, String fullName, String phone) {
        Customer customer = getById(id);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        return customerRepository.save(customer);
    }

    /**
     * Adds or replaces the customer's default delivery address.
     *
     * @param id       customer ID
     * @param street   new street
     * @param suburb   new suburb
     * @param state    new state
     * @param postcode new postcode
     */
    public void updateDefaultAddress(Long id, String street, String suburb,
                                     String state, String postcode) {
        Customer customer = getById(id);
        DeliveryAddress existing = customer.getDefaultAddress();
        if (existing != null) {
            existing.setStreet(street);
            existing.setSuburb(suburb);
            existing.setState(state);
            existing.setPostcode(postcode);
        } else {
            DeliveryAddress address = new DeliveryAddress(street, suburb, state, postcode, "Australia");
            address.setDefault(true);
            customer.addDeliveryAddress(address);
        }
        customerRepository.save(customer);
    }

    /**
     * Changes the customer's password after verifying the current one.
     *
     * @param id              customer ID
     * @param currentPassword current plain-text password
     * @param newPassword     new plain-text password
     * @throws IllegalArgumentException if the current password is incorrect
     */
    public void changePassword(Long id, String currentPassword, String newPassword) {
        Customer customer = getById(id);
        if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }
}
