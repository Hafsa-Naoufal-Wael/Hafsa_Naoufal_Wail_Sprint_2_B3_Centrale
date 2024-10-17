package com.centrale.model.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    private Long id;

    @Size(max = 20)
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "address", nullable = true)
    private String address;

    @Size(max = 255)
    @Column(name = "delivery_address", nullable = true)
    private String deliveryAddress;

    @Size(max = 50)
    @Column(name = "payment_method", nullable = true)
    private String paymentMethod;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Constructors
    public Client() {
    }

    public Client(User user, String phoneNumber, String address, String deliveryAddress, String paymentMethod) {
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Convenience methods to access User properties
    public String getFirstName() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public String getEmail() {
        return user.getEmail();
    }
}