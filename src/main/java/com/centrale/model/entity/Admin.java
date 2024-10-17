package com.centrale.model.entity;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    private Long id;

    @Min(1)
    @Max(2)
    @Column(name = "access_level", nullable = false)
    private Integer accessLevel;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Constructors
    public Admin() {
    }

    public Admin(User user, Integer accessLevel) {
        this.user = user;
        this.accessLevel = accessLevel;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
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