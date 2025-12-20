package com.example.rentapp.controller;

import com.example.rentapp.entity.User;
import com.example.rentapp.service.RentalService;
import com.example.rentapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final RentalService rentalService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService,
                             RentalService rentalService,
                             PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.rentalService = rentalService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String profile(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        populateProfileModel(model, currentUser);
        return "shared/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User userForm,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            currentUser.setFirstName(userForm.getFirstName());
            currentUser.setLastName(userForm.getLastName());
            currentUser.setEmail(userForm.getEmail());
            currentUser.setPhone(userForm.getPhone());

            userService.save(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
        }

        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
            return "redirect:/profile";
        }

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords don't match!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long!");
            return "redirect:/profile";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(currentUser);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/driver-license")
    public String updateDriverLicense(@RequestParam String driverLicenseSeries,
                                      @RequestParam String driverLicenseNumber,
                                      RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.");
            return "redirect:/profile";
        }

        if (!"CLIENT".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Только клиенты могут указывать водительские права.");
            return "redirect:/profile";
        }

        String series = driverLicenseSeries == null ? "" : driverLicenseSeries.replaceAll("\\D", "");
        String number = driverLicenseNumber == null ? "" : driverLicenseNumber.replaceAll("\\D", "");

        if (series.length() != 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Серия прав должна содержать 4 цифры.");
            return "redirect:/profile";
        }

        if (number.length() != 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Номер прав должен содержать 6 цифр.");
            return "redirect:/profile";
        }

        currentUser.setDriverLicenseSeries(series);
        currentUser.setDriverLicenseNumber(number);
        userService.save(currentUser);

        redirectAttributes.addFlashAttribute("successMessage", "Водительское удостоверение сохранено.");
        return "redirect:/profile";
    }

    @PostMapping("/card")
    public String updateCard(@RequestParam String cardNumber,
                             @RequestParam String cardExpiry,
                             @RequestParam String cardCvc,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден.");
            return "redirect:/profile";
        }

        if (!"CLIENT".equalsIgnoreCase(currentUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Только клиенты могут привязывать карту.");
            return "redirect:/profile";
        }

        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() != 16) {
            redirectAttributes.addFlashAttribute("errorMessage", "Номер карты должен содержать 16 цифр.");
            return "redirect:/profile";
        }

        String cvc = cardCvc == null ? "" : cardCvc.replaceAll("\\D", "");
        if (cvc.length() != 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "CVC код должен содержать 3 цифры.");
            return "redirect:/profile";
        }

        YearMonth expiry = parseExpiry(cardExpiry);
        if (expiry == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Укажите срок действия в формате MM/YY или MM/YYYY.");
            return "redirect:/profile";
        }
        if (expiry.isBefore(YearMonth.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Срок действия карты истёк.");
            return "redirect:/profile";
        }

        String last4 = digits.substring(digits.length() - 4);
        currentUser.setCardLast4(last4);
        currentUser.setCardExpiry(formatExpiry(expiry));
        userService.save(currentUser);

        redirectAttributes.addFlashAttribute("successMessage", "Карта успешно привязана.");
        return "redirect:/profile";
    }

    @GetMapping("/edit")
    public String redirectToProfile() {
        return "redirect:/profile";
    }

    @GetMapping("/rentals")
    public String redirectRentals() {
        return "redirect:/profile";
    }

    private void populateProfileModel(Model model, User currentUser) {
        model.addAttribute("pageTitle", "Мой профиль");
        model.addAttribute("user", currentUser);
        model.addAttribute("rentals", rentalService.findByUserId(currentUser.getId()));
    }

    private YearMonth parseExpiry(String rawExpiry) {
        if (rawExpiry == null) {
            return null;
        }
        String trimmed = rawExpiry.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        String normalized = trimmed;
        if (trimmed.length() == 5 && trimmed.charAt(2) == '/') {
            normalized = trimmed.substring(0, 2) + "/20" + trimmed.substring(3);
        }
        try {
            return YearMonth.parse(normalized, DateTimeFormatter.ofPattern("MM/yyyy"));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String formatExpiry(YearMonth expiry) {
        return expiry.format(DateTimeFormatter.ofPattern("MM/yy"));
    }
}
