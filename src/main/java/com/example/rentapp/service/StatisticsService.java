package com.example.rentapp.service;

import com.example.rentapp.entity.Rental;
import com.example.rentapp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private RentalService rentalService;

    public Map<String, Object> getDashboardStatistics(User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Rental> rentals = rentalService.findAll();
            List<Rental> rentalsForSummary = rentals.stream()
                    .filter(r -> r != null && (!"PENDING".equalsIgnoreCase(r.getStatus()) || Boolean.TRUE.equals(r.getDepositPaid())))
                    .toList();
            Map<String, Long> statusSummary = rentalService.buildStatusSummary(rentalsForSummary);

            stats.put("totalUsers", userService.getTotalUsers());
            stats.put("totalCars", carService.getTotalCars());
            stats.put("availableCars", carService.getAvailableCarsCount());
            stats.put("totalRentals", rentals.size());

            stats.put("pendingRentals", statusSummary.getOrDefault("PENDING", 0L));
            stats.put("activeRentals", statusSummary.getOrDefault("ACTIVE", 0L));
            stats.put("completedRentals", statusSummary.getOrDefault("COMPLETED", 0L));

            double avgDuration = rentals.stream()
                    .filter(r -> r != null && r.getStartDate() != null && r.getEndDate() != null)
                    .mapToLong(r -> ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()))
                    .average()
                    .orElse(0.0);
            stats.put("averageRentalDuration", Math.round(avgDuration * 100.0) / 100.0);

            long myRentals = 0;
            long myActive = 0;
            if (currentUser != null && currentUser.getId() != null) {
                try {
                    List<Rental> userRentals = rentalService.findByUserId(currentUser.getId());
                    myRentals = userRentals != null ? userRentals.size() : 0;
                    myActive = userRentals != null ? userRentals.stream()
                            .filter(r -> r != null && "ACTIVE".equalsIgnoreCase(r.getStatus()))
                            .count() : 0;
                } catch (Exception e) {
                    myRentals = 0;
                    myActive = 0;
                }
            }

            stats.put("myRentalsCount", myRentals);
            stats.put("myActiveRentals", myActive);
        } catch (Exception e) {
            stats.put("totalUsers", 0L);
            stats.put("totalCars", 0L);
            stats.put("availableCars", 0L);
            stats.put("totalRentals", 0);
            stats.put("pendingRentals", 0L);
            stats.put("activeRentals", 0L);
            stats.put("completedRentals", 0L);
            stats.put("averageRentalDuration", 0.0);
            stats.put("myRentalsCount", 0L);
            stats.put("myActiveRentals", 0L);
        }

        return stats;
    }

    public Map<String, Long> getUserStatistics() {
        Map<String, Long> userStats = new HashMap<>();
        userStats.put("totalUsers", userService.getTotalUsers());
        userStats.put("adminUsers", (long) userService.findByRole("ADMIN").size());
        userStats.put("managerUsers", (long) userService.findByRole("MANAGER").size());
        userStats.put("clientUsers", (long) userService.findByRole("CLIENT").size());
        return userStats;
    }
}
