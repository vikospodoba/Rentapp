package com.example.rentapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarDto {
    private Long id;

    @NotBlank(message = "Brand is mandatory")
    private String brand;

    @NotBlank(message = "Model is mandatory")
    private String model;

    @NotNull(message = "Year is mandatory")
    private Integer year;

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerDay;

    private String status;
    private Long categoryId;
    private String description;
    private String imageUrl;
}