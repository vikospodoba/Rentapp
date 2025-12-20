package com.example.rentapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "insurances")
@Data
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotBlank
    @Column(name = "insurance_company", length = 100, nullable = false)
    private String insuranceCompany;

    @NotBlank
    @Column(name = "policy_number", length = 50, nullable = false)
    private String policyNumber;

    @NotBlank
    @Column(name = "coverage_type", length = 100, nullable = false)
    private String coverageType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "premium_amount", nullable = false)
    private BigDecimal premiumAmount;
}

