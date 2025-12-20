package com.example.rentapp.service;

import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.Rental;
import com.example.rentapp.entity.User;
import com.example.rentapp.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private UserService userService;

    public Rental createRental(Long carId, Long userId, Rental rental) {
        Car car = carService.findById(carId)
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден"));
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        validateDates(rental.getStartDate(), rental.getEndDate());

        if (!"AVAILABLE".equalsIgnoreCase(car.getStatus())) {
            throw new RuntimeException("Автомобиль недоступен для аренды");
        }

        rental.setCar(car);
        rental.setUser(user);
        rental.setStatus("PENDING");
        rental.setDepositPaid(false);
        rental.calculateTotalPrice();

        return rentalRepository.save(rental);
    }

    public List<Rental> findByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public List<Rental> findByUserIdAndStatus(Long userId, String status) {
        return rentalRepository.findByUserIdAndStatusIgnoreCase(userId, status);
    }

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public List<Rental> findByStatus(String status) {
        return rentalRepository.findByStatusIgnoreCase(status);
    }

    public Optional<Rental> findById(Long id) {
        return rentalRepository.findById(id);
    }

    public Rental save(Rental rental) {
        return rentalRepository.save(rental);
    }

    public Rental updateRentalStatus(Long rentalId, String status) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status cannot be null or empty");
        }

        String normalizedStatus = status.trim().toUpperCase();
        rental.setStatus(normalizedStatus);

        Car car = rental.getCar();
        if (car != null && car.getId() != null) {
            try {
                switch (normalizedStatus) {
                    case "CONFIRMED", "ACTIVE" -> carService.updateCarStatus(car.getId(), "RENTED");
                    case "COMPLETED", "CANCELLED" -> carService.updateCarStatus(car.getId(), "AVAILABLE");
                    default -> {
                    }
                }
            } catch (Exception e) {
            }
        }

        return rentalRepository.save(rental);
    }

    public long getTotalRentals() {
        return rentalRepository.count();
    }

    public long countByStatus(String status) {
        return rentalRepository.findByStatusIgnoreCase(status).size();
    }

    public Map<String, Long> buildStatusSummary(List<Rental> rentals) {
        if (rentals == null || rentals.isEmpty()) {
            return new HashMap<>();
        }
        return rentals.stream()
                .filter(rental -> rental != null)
                .collect(Collectors.groupingBy(
                        rental -> rental.getStatus() == null ? "UNKNOWN" : rental.getStatus(),
                        Collectors.counting()
                ));
    }

    public void deleteRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        Car car = rental.getCar();
        if (car != null && ("CONFIRMED".equalsIgnoreCase(rental.getStatus()) || "ACTIVE".equalsIgnoreCase(rental.getStatus()))) {
            carService.updateCarStatus(car.getId(), "AVAILABLE");
        }

        rentalRepository.delete(rental);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Даты начала и окончания обязательны");
        }
        if (!startDate.isBefore(endDate)) {
            throw new RuntimeException("Дата окончания должна быть позже даты начала");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Дата начала должна быть сегодня или позже");
        }
    }
}
