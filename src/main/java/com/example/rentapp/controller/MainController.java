package com.example.rentapp.controller;

import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.User;
import com.example.rentapp.service.CarService;
import com.example.rentapp.service.StatisticsService;
import com.example.rentapp.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    private final CarService carService;
    private final StatisticsService statisticsService;
    private final UserService userService;

    private final String authorName = "Подоба Виктория Павловна";
    private final String authorGroup = "ПИ23-1";
    private final String authorUniversity = "Финансовый университет при Правительстве РФ";
    private final String projectStartDate = "01.10.2025";
    private final String projectEndDate = "20.12.2025";

    public MainController(CarService carService,
                          StatisticsService statisticsService,
                          UserService userService) {
        this.carService = carService;
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Car> availableCars = carService.findAvailableCars();
        model.addAttribute("pageTitle", "Аренда автомобилей");
        model.addAttribute("availableCars", availableCars);
        model.addAttribute("totalCars", carService.getTotalCars());
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Панель");
        model.addAttribute("currentUser", currentUser);
        Map<String, Object> stats;
        try {
            stats = statisticsService.getDashboardStatistics(currentUser);
        } catch (Exception ex) {
            stats = new HashMap<>();
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
        boolean isFirstVisit = false;
        if ("CLIENT".equalsIgnoreCase(currentUser.getRole())) {
            Long myRentalsCount = (Long) stats.getOrDefault("myRentalsCount", 0L);
            isFirstVisit = myRentalsCount == 0;
        }
        model.addAttribute("isFirstVisit", isFirstVisit);
        model.addAttribute("stats", stats);
        return "shared/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Доступ запрещён");
        return "access-denied";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "О проекте");
        model.addAttribute("author", authorName);
        model.addAttribute("group", authorGroup);
        model.addAttribute("university", authorUniversity);
        model.addAttribute("startDate", projectStartDate);
        model.addAttribute("endDate", projectEndDate);
        return "shared/about";
    }
}
