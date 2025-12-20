package com.example.rentapp.controller;

import com.example.rentapp.entity.User;
import com.example.rentapp.service.StatisticsService;
import com.example.rentapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final StatisticsService statisticsService;

    public AdminController(UserService userService,
                           StatisticsService statisticsService) {
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("pageTitle", "Управление пользователями");
        model.addAttribute("users", users);
        model.addAttribute("userStats", statisticsService.getUserStatistics());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        userService.findById(id).ifPresent(user -> {
            user.setRole(role);
            userService.save(user);
        });

        redirectAttributes.addFlashAttribute("successMessage", "User role updated successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить самого себя!");
                return "redirect:/admin/users";
            }
            
            userService.findById(id).ifPresentOrElse(
                user -> {
                    if ("admin".equalsIgnoreCase(user.getUsername())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить базового администратора!");
                    } else {
                        userService.deleteById(id);
                        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно удалён!");
                    }
                },
                () -> redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден!")
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}