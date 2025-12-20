package com.example.rentapp.controller;

import com.example.rentapp.dto.RentalRequestDto;
import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.Insurance;
import com.example.rentapp.entity.Rental;
import com.example.rentapp.entity.User;
import com.example.rentapp.service.CarService;
import com.example.rentapp.service.InsuranceService;
import com.example.rentapp.service.PaymentService;
import com.example.rentapp.service.RentalService;
import com.example.rentapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("5000");

    private final RentalService rentalService;
    private final UserService userService;
    private final CarService carService;
    private final InsuranceService insuranceService;
    private final PaymentService paymentService;

    public RentalController(RentalService rentalService,
                            UserService userService,
                            CarService carService,
                            InsuranceService insuranceService,
                            PaymentService paymentService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.carService = carService;
        this.insuranceService = insuranceService;
        this.paymentService = paymentService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listRentals(@RequestParam(required = false) String status,
                              @RequestParam(required = false) Long userId,
                              Model model) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        String normalizedStatus = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            normalizedStatus = status.trim().toUpperCase();
        }

        boolean isClient = "CLIENT".equalsIgnoreCase(currentUser.getRole());
        List<Rental> rentals;
        try {
            rentals = resolveRentals(normalizedStatus, userId, currentUser, isClient);
            if (rentals == null) {
                rentals = List.of();
            }
            if (!isClient) {
                rentals = rentals.stream()
                        .filter(r -> r != null && (!"PENDING".equalsIgnoreCase(r.getStatus()) || Boolean.TRUE.equals(r.getDepositPaid())))
                        .toList();
            }
        } catch (Exception e) {
            rentals = List.of();
        }
        Map<String, Long> statusSummary;
        try {
            statusSummary = rentalService.buildStatusSummary(rentals);
        } catch (Exception e) {
            statusSummary = new HashMap<>();
        }
        model.addAttribute("pageTitle", isClient ? "Мои аренды" : "Все аренды");
        model.addAttribute("rentals", rentals);
        model.addAttribute("statusFilter", normalizedStatus != null ? normalizedStatus : "ALL");
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("statusSummary", statusSummary);
        return "rentals/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('CLIENT')")
    public String showCreateForm(@RequestParam("carId") Long carId, Model model) {
        Car car = carService.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден"));

        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(carId);
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(3));

        model.addAttribute("pageTitle", "Аренда " + car.getFullName());
        model.addAttribute("car", car);
        model.addAttribute("rentalRequest", requestDto);
        model.addAttribute("today", LocalDate.now());
        addInsuranceInfo(model, carId);
        return "rentals/create";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('CLIENT')")
    public String createRental(@Valid @ModelAttribute("rentalRequest") RentalRequestDto rentalRequest,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Car car = carService.findById(rentalRequest.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Автомобиль не найден"));

        if (!bindingResult.hasErrors()) {
            if (rentalRequest.getStartDate() == null || rentalRequest.getEndDate() == null) {
                bindingResult.reject("dateRange", "Даты начала и окончания обязательны.");
            } else if (rentalRequest.getStartDate().isBefore(LocalDate.now())) {
                bindingResult.reject("dateRange", "Дата начала должна быть сегодня или позже.");
            } else if (!rentalRequest.getEndDate().isAfter(rentalRequest.getStartDate())) {
                bindingResult.reject("dateRange", "Дата окончания должна быть позже даты начала.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Аренда " + car.getFullName());
            model.addAttribute("car", car);
            model.addAttribute("today", LocalDate.now());
            addInsuranceInfo(model, rentalRequest.getCarId());
            return "rentals/create";
        }

        try {
            Rental rental = new Rental();
            rental.setStartDate(rentalRequest.getStartDate());
            rental.setEndDate(rentalRequest.getEndDate());
            rental.setSpecialRequests(rentalRequest.getSpecialRequests());
            Rental created = rentalService.createRental(rentalRequest.getCarId(), currentUser.getId(), rental);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка на аренду успешно создана!");
            return "redirect:/rentals/" + created.getId() + "/deposit";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cars/" + rentalRequest.getCarId();
        }
    }

    @GetMapping("/{id}/deposit")
    @PreAuthorize("hasRole('CLIENT')")
    public String showDeposit(@PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getDriverLicenseSeries() == null || currentUser.getDriverLicenseNumber() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Для оплаты депозита необходимо указать водительское удостоверение в профиле.");
            return "redirect:/profile";
        }

        if (currentUser.getCardLast4() == null || currentUser.getCardLast4().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Для оплаты депозита необходимо привязать карту. Пожалуйста, привяжите карту в профиле.");
            return "redirect:/profile";
        }

        Rental rental = rentalService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
        if (rental.getUser() == null || !rental.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (!"PENDING".equalsIgnoreCase(rental.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Эта заявка уже обработана.");
            return "redirect:/rentals";
        }

        if (Boolean.TRUE.equals(rental.getDepositPaid())) {
            redirectAttributes.addFlashAttribute("successMessage", "Депозит уже оплачен.");
            return "redirect:/rentals";
        }

        model.addAttribute("rental", rental);
        model.addAttribute("depositAmount", DEPOSIT_AMOUNT);
        return "rentals/deposit";
    }

    @PostMapping("/{id}/deposit/confirm")
    @PreAuthorize("hasRole('CLIENT')")
    public String confirmDeposit(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (currentUser.getDriverLicenseSeries() == null || currentUser.getDriverLicenseNumber() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Для оплаты депозита необходимо указать водительское удостоверение в профиле.");
            return "redirect:/profile";
        }

        if (currentUser.getCardLast4() == null || currentUser.getCardLast4().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Для оплаты депозита необходимо привязать карту. Пожалуйста, привяжите карту в профиле.");
            return "redirect:/profile";
        }

        Rental rental = rentalService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
        if (rental.getUser() == null || !rental.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (!"PENDING".equalsIgnoreCase(rental.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Эта заявка уже обработана.");
            return "redirect:/rentals";
        }

        if (Boolean.TRUE.equals(rental.getDepositPaid())) {
            redirectAttributes.addFlashAttribute("successMessage", "Депозит уже оплачен.");
            return "redirect:/rentals";
        }

        rental.setDepositPaid(true);
        rentalService.save(rental);
        paymentService.createPayment(rental.getId(), DEPOSIT_AMOUNT, "DEPOSIT");

        redirectAttributes.addFlashAttribute("successMessage", "Депозит успешно оплачен.");
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/deposit/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public String cancelDeposit(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Rental rental = rentalService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
        if (rental.getUser() == null || !rental.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (!"PENDING".equalsIgnoreCase(rental.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Эта заявка уже обработана.");
            return "redirect:/rentals";
        }

        rentalService.updateRentalStatus(id, "CANCELLED");
        redirectAttributes.addFlashAttribute("successMessage", "Заявка отменена.");
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancelRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.updateRentalStatus(id, "CANCELLED");
            redirectAttributes.addFlashAttribute("successMessage", "Rental cancelled successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String confirmRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rental not found"));
            if (!Boolean.TRUE.equals(rental.getDepositPaid())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Сначала требуется подтверждение депозита клиентом.");
                return "redirect:/rentals";
            }
            rentalService.updateRentalStatus(id, "CONFIRMED");
            redirectAttributes.addFlashAttribute("successMessage", "Rental confirmed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'CLIENT')")
    public String completeRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.updateRentalStatus(id, "COMPLETED");
            redirectAttributes.addFlashAttribute("successMessage", "Rental marked as completed!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRental(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rentalService.deleteRental(id);
            redirectAttributes.addFlashAttribute("successMessage", "Аренда успешно удалена!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rentals";
    }

    private List<Rental> resolveRentals(String status,
                                        Long userId,
                                        User currentUser,
                                        boolean isClient) {
        if (isClient) {
            return status != null
                    ? rentalService.findByUserIdAndStatus(currentUser.getId(), status)
                    : rentalService.findByUserId(currentUser.getId());
        }

        if (userId != null) {
            return status != null
                    ? rentalService.findByUserIdAndStatus(userId, status)
                    : rentalService.findByUserId(userId);
        }

        if (status != null) {
            return rentalService.findByStatus(status);
        }

        return rentalService.findAll();
    }

    private void addInsuranceInfo(Model model, Long carId) {
        Insurance insurance = insuranceService.findByCarId(carId).stream()
                .findFirst()
                .orElse(null);
        model.addAttribute("insuranceInfo", insurance);
    }
}
