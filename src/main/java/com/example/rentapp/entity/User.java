package com.example.rentapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_expiry", length = 7)
    private String cardExpiry;

    @Column(name = "driver_license_series", length = 4)
    private String driverLicenseSeries;

    @Column(name = "driver_license_number", length = 6)
    private String driverLicenseNumber;

    @Column(nullable = false)
    private String role = "CLIENT";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Rental> rentals = new ArrayList<>();

    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
