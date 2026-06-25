package com.shadentcg.shop.service;

import com.shadentcg.shop.model.Customer;
import com.shadentcg.shop.model.DeliveryAddress;
import com.shadentcg.shop.model.GuestSession;
import com.shadentcg.shop.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(String fullName, String email, String rawPassword, String phone, String street, String suburb, String state, String postcode) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists.");
        }
        Customer customer = new Customer(fullName, email, passwordEncoder.encode(rawPassword), phone);
        DeliveryAddress address = new DeliveryAddress(street, suburb, state, postcode, "Australia");
        address.setDefault(true);
        customer.addDeliveryAddress(address);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    public Customer updateProfile(Long id, String fullName, String phone) {
        Customer customer = getById(id);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        return customerRepository.save(customer);
    }

    public void updateDefaultAddress(Long id, String street, String suburb, String state, String postcode) {
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

    public void changePassword(Long id, String currentPassword, String newPassword) {
        Customer customer = getById(id);
        if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }
}
