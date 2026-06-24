package com.shadentcg.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Data-holder for a customer's delivery address (A2 data-holder class).
 *
 * <p>Responsibilities (from A2 CRC card):
 * <ul>
 *   <li>Hold structured address fields (street, suburb, state, postcode, country)</li>
 *   <li>Provide formatted address string for display and shipment records</li>
 * </ul>
 *
 * <p>A customer may have 1..* DeliveryAddresses; the first is treated as default.
 */
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank(message = "Street address is required")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "Suburb is required")
    @Column(nullable = false)
    private String suburb;

    @NotBlank(message = "State is required")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Postcode is required")
    @Column(nullable = false)
    private String postcode;

    @Column(nullable = false)
    private String country = "Australia";

    @Column(nullable = false)
    private boolean isDefault = false;

    // ── Constructors ──────────────────────────────────────────────────

    public DeliveryAddress() {}

    public DeliveryAddress(String street, String suburb, String state,
                           String postcode, String country) {
        this.street   = street;
        this.suburb   = suburb;
        this.state    = state;
        this.postcode = postcode;
        this.country  = country;
    }

    // ── Business logic ────────────────────────────────────────────────

    /**
     * Returns the full formatted address string used in orders and shipments.
     *
     * @return e.g. "123 Main St, Hawthorn VIC 3122, Australia"
     */
    public String getFormattedAddress() {
        return street + ", " + suburb + " " + state + " " + postcode + ", " + country;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getSuburb() { return suburb; }
    public void setSuburb(String suburb) { this.suburb = suburb; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
