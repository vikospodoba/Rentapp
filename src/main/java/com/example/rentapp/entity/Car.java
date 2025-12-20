package com.example.rentapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "cars")
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand is mandatory")
    private String brand;

    @NotBlank(message = "Model is mandatory")
    private String model;

    @NotNull(message = "Year is mandatory")
    @Column(name = "year_value")
    private Integer year;

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "price_per_day")
    private BigDecimal pricePerDay;

    private String status = "AVAILABLE";

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CarCategory category;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    public String getFullName() {
        return brand + " " + model + " (" + year + ")";
    }
}
