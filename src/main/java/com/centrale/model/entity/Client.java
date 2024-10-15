package com.centrale.model.entity;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "clients")
@PrimaryKeyJoinColumn(name = "id")
public class Client extends User {

    @NotBlank
    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    private Long id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;

    // Constructors

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
