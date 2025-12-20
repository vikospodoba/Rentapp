package com.example.rentapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RentalRequestDto {

    @NotNull(message = "ID автомобиля обязателен")
    private Long carId;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDate endDate;

    private String specialRequests;

    public boolean isValidDateRange() {
        return startDate != null && endDate != null &&
                !startDate.isAfter(endDate) &&
                !startDate.isBefore(LocalDate.now());
    }

    public long getRentalDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }
}