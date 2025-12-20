package com.example.rentapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rentals")
@Data
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Start date is mandatory")
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private String status = "PENDING";

    @Column(name = "deposit_paid")
    private Boolean depositPaid = false;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "rental", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (startDate != null && endDate != null && car != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            if (days <= 0) {
                days = 1;
            }
            this.totalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));
        }
    }

    @Transient
    public long getRentalDays() {
        if (startDate != null && endDate != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            return Math.max(days, 1);
        }
        return 0;
    }
}
